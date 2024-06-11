package com.weedshop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.weedshop.utils.Constant;
import com.weedshop.utils.NotificationUtils;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import net.simonvt.menudrawer.MenuDrawer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;

public class MapActivity extends SideMenuActivity implements
        OnMapReadyCallback {

    private static final int REQUEST_LOCATION = 1;
    private static final int REQUEST_PHONE_CALL = 2;

    private static String[] PERMISSIONS_LOCATION = {ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION};
    private static String[] PERMISSIONS_CALL = {CALL_PHONE};
    public static String TAG = MapActivity.class.getSimpleName();
    LinearLayout llDetail;
    RelativeLayout llMessage;
    TextView txtMessage;
    SpinKitView spin_kit;

    LatLng latLng;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    Marker driverMarker = null;
    private float zoomLevel = 0;
    // for bottom view
    private TextView txtdeliveryshopadd;
    private TextView txtdeliveryadd;
    private TextView txtkm;
    private TextView txttime;
    private TextView txtprice;
    private TextView txtfname;
    private TextView txtcarNumber;
    private TextView txtOrderId;
    private CircleImageView imgprofile;
    private FrameLayout frmdeliverphone;
    String order_idIntent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpMenu(this, R.layout.activity_main_maps);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);

        llDetail = (LinearLayout) findViewById(R.id.llDetail);
        llMessage = (RelativeLayout) findViewById(R.id.llMessage);
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        spin_kit = (SpinKitView) findViewById(R.id.spin_kit);

        mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mFragment.getMapAsync(MapActivity.this);
        rlCart.setVisibility(View.INVISIBLE);
        mMenuDrawerRight.setTouchMode(MenuDrawer.TOUCH_MODE_NONE);
        txt_screen_title.setText("Your Current Location");

        findViewById(R.id.txtBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        txtdeliveryshopadd = (TextView) findViewById(R.id.txtdeliveryshopadd);
        txtdeliveryadd = (TextView) findViewById(R.id.txtdeliveryadd);
        txtkm = (TextView) findViewById(R.id.txtkm);
        txttime = (TextView) findViewById(R.id.txttime);
        txtprice = (TextView) findViewById(R.id.txtprice);
        txtfname = (TextView) findViewById(R.id.txtfname);
        txtcarNumber = (TextView) findViewById(R.id.txtcarNumber);
        txtOrderId = (TextView) findViewById(R.id.txtOrderId);
        imgprofile = (CircleImageView) findViewById(R.id.imgprofile);

        frmdeliverphone = (FrameLayout) findViewById(R.id.frmdeliverphone);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.containsKey("order_id")) {
                order_idIntent = b.getString("order_id");
                txtOrderId.setText("Order id : " + b.getString("orderCode"));
                txt_screen_title.setText(b.getString("productName"));
            }
        }

        txtOrderId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(order_idIntent)) {
                    Intent intent = new Intent(MapActivity.this, OrderDetailActivity.class);
                    intent.putExtra("orderId", order_idIntent);
                    startActivity(intent);
                }
            }
        });


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("LocationUpdate"));

        if (!isServiceRunning()) {
            Intent i = new Intent(MapActivity.this, MyLocationService.class);
            startService(i);
        }

        callApiToGetUserOnMap();
    }

    void callApiToGetUserOnMap() {
        llDetail.setVisibility(View.GONE);
        llMessage.setVisibility(View.VISIBLE);
        spin_kit.setVisibility(View.VISIBLE);

        CommonTask task = null;
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", sharedpreferences.getString(Pref.id, ""));
        // map.put("order_id", order_idIntent);
        task = new CommonTask(MapActivity.this, Constant.current_user_on_map_status_api, map, new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result, int requestCode) {
                if (result != null
                        && !TextUtils.isEmpty(result)
                        ) {
                    try {
                        JSONArray jsonArrayData = null;
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.getString("response") != null && jsonObject.getString("response").equalsIgnoreCase("true")) {
                            jsonArrayData = jsonObject.getJSONArray("data");
                            if (jsonArrayData != null
                                    && jsonArrayData.length() > 0
                                    ) {
                                for (int i = 0; i < jsonArrayData.length(); i++) {
                                    if (i == 0) {
                                        JSONObject jsonObject1 = jsonArrayData.getJSONObject(i);
                                        //draw lines on maps
                                        if (jsonObject1 != null) {
                                            txtOrderId.setText("Order id : " + jsonObject1.getString("order_code"));
                                            order_idIntent = jsonObject1.getString("id");

                                            String driver_id = jsonObject1.getString("driver_id");
                                            sharedpreferences.edit().putString("driver_id", driver_id).commit();
                                            String orderStatus = jsonObject1.getString("status");
                                            if (orderStatus.equalsIgnoreCase("Pending")) {
                                                txtMessage.setText("Nearby Drivers Are Being Contacting");
                                                spin_kit.setVisibility(View.VISIBLE);
                                                llDetail.setVisibility(View.GONE);
                                                llMessage.setVisibility(View.VISIBLE);
                                                setCurrentShopOrderDetail(jsonObject1);
                                            } else {
                                                llDetail.setVisibility(View.VISIBLE);
                                                llMessage.setVisibility(View.GONE);
                                                setCurrentDeliveryOrderDetail(jsonObject1);
                                                setCurrentShopOrderDetail(jsonObject1);
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            llDetail.setVisibility(View.GONE);
                            llMessage.setVisibility(View.VISIBLE);
                            txtMessage.setText("No any current order found!");
                            spin_kit.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, true);
        task.executeAsync();
    }

    // Our handler for received Intents. This will be called whenever an Intent
// with an action named "LocationUpdate" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONArray jsonArrayData = null;
                JSONObject jsonObject = new JSONObject(intent.getStringExtra("response"));
                if (jsonObject.getString("response") != null && jsonObject.getString("response").equalsIgnoreCase("true")) {
                    jsonArrayData = jsonObject.getJSONArray("data");
                    if (jsonArrayData != null
                            && jsonArrayData.length() > 0
                            ) {
                        for (int i = 0; i < jsonArrayData.length(); i++) {
                            if (i == 0) {
                                JSONObject jsonObject1 = jsonArrayData.getJSONObject(i);
                                //draw lines on maps
                                if (jsonObject1 != null) {
                                    String driver_id = jsonObject1.getString("driver_id");
                                    sharedpreferences.edit().putString("driver_id", driver_id).commit();
                                    String orderStatus = jsonObject1.getString("status");
                                    if (!orderStatus.equalsIgnoreCase("Pending")) {
                                        // Get extra data included in the Intent
                                        if (llDetail.getVisibility() == View.VISIBLE) {
                                            LatLng driver = new LatLng(jsonObject1.getDouble("driver_latitude"), jsonObject1.getDouble("driver_longitude"));
                                            moveMarker(driver.latitude, driver.longitude);
                                        } else {
                                            mGoogleMap.clear();
                                            llDetail.setVisibility(View.VISIBLE);
                                            llMessage.setVisibility(View.GONE);
                                            setCurrentDeliveryOrderDetail(jsonObject1);
                                            setCurrentShopOrderDetail(jsonObject1);
                                        }
                                    } else {

                                    }
                                }
                            }
                        }
                    }
                } else {
                    mGoogleMap.clear();
                    llDetail.setVisibility(View.GONE);
                    llMessage.setVisibility(View.VISIBLE);
                    txtMessage.setText("No any current order found!");
                    txtOrderId.setText("");
                    spin_kit.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        try {
            if (isServiceRunning()) {
                Intent i = new Intent(MapActivity.this, MyLocationService.class);
                stopService(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

        MapStyleManager styleManager = MapStyleManager.attachToMap(this, mGoogleMap);
        styleManager.addStyle(0, R.raw.map_style_silver_sparse);

    }

    private void setCurrentShopOrderDetail(final JSONObject jsonObject) {
        try {
            if (TextUtils.isEmpty(jsonObject.getString("driver_latitude")) || jsonObject.getString("driver_latitude").equalsIgnoreCase("null"))
                return;

            LatLng origin = new LatLng(jsonObject.getDouble("driver_latitude"), jsonObject.getDouble("driver_longitude"));
            LatLng dest = new LatLng(jsonObject.getDouble("store_latitude"), jsonObject.getDouble("store_longitude"));
//            greenRawLine(origin, dest);
            moveMarker(dest.latitude, dest.longitude, "Shop", R.drawable.pin_black, false);
            moveMarker(origin.latitude, origin.longitude, "Driver", R.drawable.pin_green, false);
            String url = getDirectionsUrl(origin, dest);
            DownloadTask1 downloadTask = new DownloadTask1();
            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
            final String order_id = jsonObject.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentDeliveryOrderDetail(JSONObject jsonObject) {
        try {
            txtdeliveryshopadd.setText(jsonObject.getString("address"));
            txtdeliveryadd.setText(jsonObject.getString("delivery_address"));

            double distance = Double.parseDouble(jsonObject.getString("distance"));
            txtkm.setText(new DecimalFormat("##.##").format(distance) + " km");

            txttime.setText(jsonObject.getString("time"));
            txtprice.setText("$" + numberFormat.format(Float.parseFloat(jsonObject.getString("final_total"))));
            txtfname.setText(jsonObject.getString("driver_name"));

            String image_url = jsonObject.getString("image_url");
           /* if (!TextUtils.isEmpty(image_url))
                Glide.with(this).load(image_url).placeholder(R.drawable.profile_img).into(imgprofile);*/

            if (!TextUtils.isEmpty(image_url)) {
                Glide.with(this).load(image_url).placeholder(R.drawable.profile_img).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        //  Log.e("Glide", "onException" + e.getMessage());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.e("Glide", "onResourceReady Success.");
                        imgprofile.setImageResource(0);
                        imgprofile.setBackgroundResource(0);
                        imgprofile.setImageDrawable(resource);
                        return false;
                    }
                }).into(imgprofile);
            }

            final String delivery_phone = jsonObject.getString("mobile");
            final String car_number = jsonObject.getString("car_number");
            txtcarNumber.setText(car_number);
            frmdeliverphone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasPermission(CALL_PHONE)) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + delivery_phone));
                        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        startActivity(intent);
                    } else {
                        requestCallPermissions();
                    }
                }
            });
            LatLng origin = new LatLng(jsonObject.getDouble("store_latitude"), jsonObject.getDouble("store_longitude"));

            LatLng dest = new LatLng(jsonObject.getDouble("delivery_latitude"), jsonObject.getDouble("delivery_longitude"));
           /* GPSTracker gpsTracker = new GPSTracker(MapActivity.this);
            LatLng dest = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());*/
            moveMarker(dest.latitude, dest.longitude, "You", R.drawable.pin_cyan, true);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API

            final String price = jsonObject.getString("final_total");
            downloadTask.execute(url);

            final String order_id = jsonObject.getString("id");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        Log.e("draw url", url);
        return url;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private class DownloadTask1 extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask1 parserTask = new ParserTask1();
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLACK);
                lineOptions.geodesic(true);
            }
            if (mGoogleMap != null && lineOptions != null) {
// Drawing polyline in the Google Map for the i-th route
                mGoogleMap.addPolyline(lineOptions);
            }
        }
    }

    private class ParserTask1 extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(getResources().getColor(R.color.map_path));
                lineOptions.geodesic(true);
            }
            if (mGoogleMap != null && lineOptions != null) {
// Drawing polyline in the Google Map for the i-th route
                mGoogleMap.addPolyline(lineOptions);
            }
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private static LatLng currentLatLng;

    private void moveMarker(double lat, double lng, String name, int res, boolean animate) {
        latLng = new LatLng(lat, lng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.snippet(name);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(res));
        // clear previous marker when location change

        if (driverMarker != null) {
            driverMarker.remove();
        }

        if (name.equalsIgnoreCase("Driver")) {
            driverMarker = mGoogleMap.addMarker(markerOptions);
        } else {
            mGoogleMap.addMarker(markerOptions);
        }

        //Move the camera to the user's location and zoom in!
        if (animate)
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
    }

    private void moveMarker(double lat, double lng) {
        latLng = new LatLng(lat, lng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.snippet("Driver");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_green));
        // clear previous marker when location change

        if (driverMarker != null) {
            driverMarker.remove();
        }
        driverMarker = mGoogleMap.addMarker(markerOptions);

        if (zoomLevel == 0) {
            zoomLevel = 15.0f;
        } else {
            zoomLevel = mGoogleMap.getCameraPosition().zoom;
        }
        //Move the camera to the user's location and zoom in!

        //move map camera
       /* mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));*/
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }

    /**
     * Requests the ACCESS_COARSE_LOCATION permissions.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    @SuppressLint("NewApi")
    private void requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, ACCESS_FINE_LOCATION)) {
            requestPermissions(PERMISSIONS_LOCATION, REQUEST_LOCATION);
        } else {
            ActivityCompat.requestPermissions(MapActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }
    }

    @SuppressLint("NewApi")
    private void requestCallPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, CALL_PHONE)) {
            requestPermissions(PERMISSIONS_CALL, REQUEST_PHONE_CALL);
        } else {
            ActivityCompat.requestPermissions(MapActivity.this, PERMISSIONS_CALL, REQUEST_PHONE_CALL);
        }
    }

    @SuppressLint("NewApi")
    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    /*Permissions*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //  showToast("Permission Granted, Now you can access camera.");
                } else {
                    // showToast("Permission Denied, You cannot access camera.");
                    //code for deny
                    showDialogNotCancelable("Permission", "Please Grant Permissions to show location.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestLocationPermissions();
                        }
                    });
                }
                break;
            case REQUEST_PHONE_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //  showToast("Permission Granted, Now you can access camera.");
                } else {
                    // showToast("Permission Denied, You cannot access camera.");
                    //code for deny
                    showDialogNotCancelable("Permission", "Please Grant Permissions to Call phone.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestCallPermissions();
                        }
                    });
                }
                break;
        }
    }

    private void showDialogNotCancelable(String title, String message, DialogInterface.OnClickListener okListener) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setCancelable(false)
                .create()
                .show();
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.weedshop.MyLocationService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

