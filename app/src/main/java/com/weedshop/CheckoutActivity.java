package com.weedshop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.transitionseverywhere.TransitionManager;
import com.weedshop.Adapter.CheckoutAdapter;
import com.weedshop.Adapter.ItemEditClickListener;
import com.weedshop.model.Address;
import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Constant;
import com.weedshop.utils.GeocodingLocation;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckoutActivity extends SideMenuActivity implements OnTaskCompleted, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener, ItemEditClickListener {
    private static final int COMMON_REQUEST_CODE = 4000;
    private static final int GET_ADDRESS_REQUEST_CODE = 4001;
    private static final int ADD_ADDRESS_REQUEST_CODE = 4002;
    private ListView listCheckOut;
    private CheckoutAdapter adapter;
    private int click = 0, zoom = 0;
    private final String PREFIX = "+1";
    private TextView txt_Add_other_add, txtBack, tvTotal;
    private ScrollView scroll_add_other_order;
    private ImageView imgclose, ivBack, iv_correct, img_marker;
    private Button btn_place_order, btn_add_address;
    private Spinner sp_region;
    private ArrayList<String> regionList;
    private String region = "";
    private GoogleMap googleMap;
    private Marker marker;
    private LatLng latLng;
    private GPSTracker tracker;
    private Location location;
    private EditText et_name, et_lname, et_address, et_city, et_zipcode, et_state, et_phone;
    private ArrayList<Address> addressList;
    private boolean isUpdateAddress = false;
    private String addressID;
    private RelativeLayout rlMap;
    private ViewGroup transitionsContainer;
    private boolean isGeoCoderLoading = false;
    private StringBuilder addressBuilder;
    private TextView txtDisclaimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpMenu(this, R.layout.activity_checkout);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        txt_screen_title.setText("Checkout");
        transitionsContainer = (ViewGroup) findViewById(R.id.transitions_container);
        listCheckOut = (ListView) findViewById(R.id.lst_checkout);
        btn_place_order = (Button) findViewById(R.id.btn_place_order);
        btn_add_address = (Button) findViewById(R.id.btn_add_address);
        scroll_add_other_order = (ScrollView) findViewById(R.id.scroll_add_other_order);
        txt_Add_other_add = (TextView) findViewById(R.id.txt_Add_other_add);
        txtBack = (TextView) findViewById(R.id.txtBack);
        tvTotal = (TextView) findViewById(R.id.tvTotal);
        imgclose = (ImageView) findViewById(R.id.imgclose);
        iv_correct = (ImageView) findViewById(R.id.iv_correct);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        sp_region = (Spinner) findViewById(R.id.sp_region);
        et_name = (EditText) findViewById(R.id.et_name);
        et_lname = (EditText) findViewById(R.id.et_lname);
        et_address = (EditText) findViewById(R.id.et_address);
        et_city = (EditText) findViewById(R.id.et_city);
        et_zipcode = (EditText) findViewById(R.id.et_zipcode);
        et_state = (EditText) findViewById(R.id.et_state);
        et_phone = (EditText) findViewById(R.id.et_phone);
        img_marker = (ImageView) findViewById(R.id.img_marker);
        rlMap = (RelativeLayout) findViewById(R.id.rlMap);
        txtDisclaimer = (TextView) findViewById(R.id.txtDisclaimer);
        txtDisclaimer.setText(CommonUtils.getSpannableString(txtDisclaimer.getText().toString()));
        et_phone.setText(PREFIX);
        Selection.setSelection(et_phone.getText(), et_phone.getText().length());

        tvTotal.setText(getIntent().getStringExtra("total"));
        // et_phone.setText(PREFIX + sharedpreferences.getString(Pref.mobile, ""));

        tracker = new GPSTracker(this);
        location = tracker.getLocation();

        HashMap<String, String> map = new HashMap<>();
        map.put("type", "region");
        CommonTask task = new CommonTask(CheckoutActivity.this, Constant.common, map, COMMON_REQUEST_CODE, false);
        task.executeAsync();

        // Obtain the MapFragment and set the async listener to be notified when the map is ready.
        WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapFragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scroll_add_other_order.requestDisallowInterceptTouchEvent(true);
            }
        });

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("user_id", sharedpreferences.getString(Pref.id, ""));
        params.put("action", "list");

        CommonTask handler = new CommonTask(CheckoutActivity.this, Constant.user_addresses, params, GET_ADDRESS_REQUEST_CODE, true);
        handler.executeAsync();

        sp_region.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (regionList != null) {
                    region = regionList.get(position);
                    getAddressString();
                    if (position == 0) {
                        region = "";
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        iv_correct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click == 0) {
                    iv_correct.setImageResource(R.drawable.img_terms);
                    click = 1;
                } else if (click == 1) {
                    iv_correct.setImageResource(R.drawable.round_corner_terms);
                    click = 0;
                }
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        txt_Add_other_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoom = 0;
                if (!isUpdateAddress) {
                    et_name.setText("");
                    et_lname.setText("");
                    et_address.setText("");
                    et_city.setText("");
                    et_zipcode.setText("");
                    sp_region.setSelection(0);
                    et_phone.setText(PREFIX + sharedpreferences.getString(Pref.mobile, ""));

                    if (tracker.canGetLocation() && googleMap != null) {
                        location = tracker.getLocation();
                        if (location != null) {
                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                        }
                    }
                }
                Animation bottomUp = AnimationUtils.loadAnimation(CheckoutActivity.this,
                        R.anim.bottom_up);
                ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.scroll_add_other_order);
                hiddenPanel.startAnimation(bottomUp);
                hiddenPanel.setVisibility(View.VISIBLE);
            }
        });

        imgclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.hideKeyboard(CheckoutActivity.this);
                isUpdateAddress = false;
                Animation bottomUp = AnimationUtils.loadAnimation(CheckoutActivity.this,
                        R.anim.bottom_down);
                ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.scroll_add_other_order);
                hiddenPanel.startAnimation(bottomUp);
                hiddenPanel.setVisibility(View.GONE);
            }
        });

        btn_place_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter.getSelectedAddress() >= 0) {
                    Address address = addressList.get(adapter.getSelectedAddress());
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Pref.lastAddress, address.id);
                    editor.apply();
                    Intent intent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
                    intent.putExtra("address", address);
                    startActivity(intent);
                } else {
                    showToast("Please select Address.");
                }
            }
        });

        txtDisclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String disclaimer = "file:///android_asset/disclaimer.html";
                Intent intent = new Intent(CheckoutActivity.this, PrivacyPolicyActivity.class);
                intent.putExtra("url", disclaimer);
                intent.putExtra("title", "Legal Disclaimer");
                startActivity(intent);
            }
        });

        btn_add_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = et_name.getText().toString().trim();
                String lname = et_lname.getText().toString().trim();
                String streetAddress = et_address.getText().toString().trim();
                String city = et_city.getText().toString().trim();
                String zipcode = et_zipcode.getText().toString().trim();
                String state = et_state.getText().toString().trim();
                String phone = et_phone.getText().toString().trim();

                if (phone.length() > 3) {
                    phone = phone.substring(2);
                }

                if (TextUtils.isEmpty(name)) {
                    showToast("Please enter first name.");
                } else if (TextUtils.isEmpty(lname)) {
                    showToast("Please enter last name.");
                } else if (TextUtils.isEmpty(streetAddress)) {
                    showToast("Please enter address.");
                } else if (TextUtils.isEmpty(city)) {
                    showToast("Please enter city.");
                } else if (TextUtils.isEmpty(zipcode)) {
                    showToast("Please enter zipcode.");
                } else if (TextUtils.isEmpty(region)) {
                    showToast("Please select region.");
                } else if (TextUtils.isEmpty(phone)) {
                    showToast("Please enter phone.");
                } else if (click == 0 || latLng == null) {
                    showToast("Please select location.");
                } else {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("user_id", sharedpreferences.getString(Pref.id, ""));
                    if (isUpdateAddress) {
                        params.put("action", "edit");

                        params.put("address_id", addressID);
                    } else {
                        params.put("action", "add");
                    }
                    params.put("firstname", name);
                    params.put("lastname", lname);
                    params.put("address", streetAddress);
                    params.put("city", city);
                    params.put("region", region);
                    params.put("zipcode", zipcode);
                    params.put("state", state);
                    params.put("phone", phone);
                    params.put("latitude", latLng.latitude + "");
                    params.put("longitude", latLng.longitude + "");
                   /* params.put("latitude", marker.getPosition().latitude + "");
                    params.put("longitude", marker.getPosition().longitude + "");*/

                    CommonTask handler = new CommonTask(CheckoutActivity.this, Constant.user_addresses, params, ADD_ADDRESS_REQUEST_CODE, true);
                    handler.executeAsync();
                }
            }
        });

        et_phone.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().contains(PREFIX)) {
                    et_phone.setText(PREFIX);
                    Selection.setSelection(et_phone.getText(), et_phone.getText().length());
                }
            }
        });

        et_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getAddressString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_city.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getAddressString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_zipcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getAddressString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (TextUtils.isEmpty(result)) return;
        if (GET_ADDRESS_REQUEST_CODE == requestCode) {
            try {
                addressList = new ArrayList<>();
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Address address = new Address();
                        address.id = object.getString("id");
                        address.user_id = object.getString("user_id");
                        address.firstname = object.getString("firstname");
                        address.lastname = object.getString("lastname");
                        address.address = object.getString("address");
                        //address.street = object.getString("street");
                        address.region = object.getString("region");
                        address.city = object.getString("city");
                        address.zipcode = object.getString("zipcode");
                        address.phone = object.getString("phone");
                        address.country = object.getString("country");
                        address.state = object.getString("state");
                        address.latitude = object.getString("latitude");
                        address.longitude = object.getString("longitude");
                        address.status = object.getString("status");
                        addressList.add(address);
                    }
                    adapter = new CheckoutAdapter(CheckoutActivity.this, addressList);
                    listCheckOut.setAdapter(adapter);
                } else {
                    showToast(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ADD_ADDRESS_REQUEST_CODE == requestCode) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {
                    if (!isUpdateAddress) {
                        showToast("New Address added.");
                    } else {
                        showToast("Address updated.");
                    }
                    imgclose.callOnClick();
                    isUpdateAddress = false;

                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("user_id", sharedpreferences.getString(Pref.id, ""));
                    params.put("action", "list");

                    CommonTask handler = new CommonTask(CheckoutActivity.this, Constant.user_addresses, params, GET_ADDRESS_REQUEST_CODE, true);
                    handler.executeAsync();
                } else {
                    showToast(msg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (COMMON_REQUEST_CODE == requestCode) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {
                    regionList = new ArrayList<>();
                    regionList.add("Select Region");
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        regionList.add(object.getString("name"));
                    }
                    ArrayAdapter regionAdapter = new ArrayAdapter(this, R.layout.spinner_text_item_add, regionList);
                    sp_region.setAdapter(regionAdapter);
                } else {
                    showToast(msg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Map is ready to be used.
        this.googleMap = googleMap;
        this.googleMap.setOnMarkerDragListener(this);
        this.googleMap.setOnMapClickListener(this);

        if (tracker.canGetLocation() && tracker.getLocation() != null) {
            location = tracker.getLocation();
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }

        MapStyleManager styleManager = MapStyleManager.attachToMap(this, this.googleMap);
        styleManager.addStyle(0, R.raw.map_style_silver_sparse);

        this.googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                CheckoutActivity.this.googleMap.clear();
                latLng = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                Log.e("" + latLng.latitude, "" + latLng.longitude);
            }
        });
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        this.marker = marker;
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        this.marker = marker;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8f));
        int size = 150;
        if (zoom == 0) {
            zoom = 1;
            size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());
        } else {
            zoom = 0;
            size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
        }
       /* TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new Fade());
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.setDuration(1000);
        TransitionManager.beginDelayedTransition(transitionsContainer, transitionSet);*/

        TransitionManager.beginDelayedTransition(transitionsContainer);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rlMap.getLayoutParams();
        params.height = size;
        rlMap.setLayoutParams(params);
    }

    @Override
    public void onItemEditClick(View v, int position) {

        isUpdateAddress = true;
        Address address = addressList.get(position);
        region = address.region;
        addressID = address.id;
        txt_Add_other_add.callOnClick();
        et_name.setText(address.firstname);
        et_lname.setText(address.lastname);
        et_address.setText(address.address);
        et_city.setText(address.city);
        et_zipcode.setText(address.zipcode);
        et_phone.setText(PREFIX + address.phone);

        try {
            if (sp_region != null && regionList != null)
                sp_region.setSelection(regionList.indexOf(address.region));

        } catch (Exception e) {
            e.printStackTrace();
        }

        latLng = new LatLng(Double.parseDouble(address.latitude), Double.parseDouble(address.longitude));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
    }

    @Override
    public void onBackPressed() {
        if (scroll_add_other_order.getVisibility() == View.VISIBLE) {
            imgclose.callOnClick();
            isUpdateAddress = false;
            return;
        }
        super.onBackPressed();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    isGeoCoderLoading = false;
                    Bundle bundle = message.getData();
                    if (bundle != null && !TextUtils.isEmpty(bundle.getString("address"))) {
                        latLng = new LatLng(Double.parseDouble(bundle.getString("latitude")), Double.parseDouble(bundle.getString("longitude")));
                    } else {
                        Log.e("Address Res:", "No address found!");
                       /* if (tracker.canGetLocation()) {
                            location = tracker.getLocation();
                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                        }*/
                    }
                    break;
                default:
            }
            if (latLng != null && googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            }
        }
    }

    private void getAddressString() {
        String streetAddress = et_address.getText().toString().trim();
        String city = et_city.getText().toString().trim();
        String zipcode = et_zipcode.getText().toString().trim();
        String state = et_state.getText().toString().trim();

        if (TextUtils.isEmpty(streetAddress)) return;
        addressBuilder = new StringBuilder();
        addressBuilder.append(streetAddress).append(",").append(city).append(",").append(region).append(",").append(state).append(",").append(zipcode);
        Log.e("addressBuilder", addressBuilder.toString());
        if (!isGeoCoderLoading) {
            isGeoCoderLoading = true;
            GeocodingLocation locationAddress = new GeocodingLocation();
            locationAddress.getAddressFromLocation(addressBuilder.toString(),
                    getApplicationContext(), new GeocoderHandler());
        }
    }
}