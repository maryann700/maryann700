package com.weedshop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;
import com.weedshop.custom.YesNoAlertDialogInterface;
import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Constant;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.webservices.CommonTask;

import net.simonvt.menudrawer.MenuDrawer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by MTPC_51 on 4/18/2017.
 */

public class OrderDetailActivity extends SideMenuActivity implements OnTaskCompleted {
    private ViewGroup transitionsContainer;
    private TextView txtorderdate;
    private TextView txtorderno;
    private TextView txtordertotal;
    private TextView txtshipname, txtshopname;
    private TextView txtphoneno1, txtshopphoneno1;
    private TextView txtaddress1, txtshopaddress1;
    private TextView txtitems;
    private TextView txtdeliverycharge;
    private TextView txtordertotalsummery;
    private LinearLayout lnrsipment;
    private TextView txtviewmore;
    private String orderId;

    private enum API_CALL {
        REORDER_API_CALL, DETAILS_API_CALL
    }

    private API_CALL mEnum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpMenu(this, R.layout.activity_order_detail);

        rlCart.setVisibility(View.INVISIBLE);
        mMenuDrawerRight.setTouchMode(MenuDrawer.TOUCH_MODE_NONE);

        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        txt_screen_title.setText("View order details");
        orderId = getIntent().getStringExtra("orderId");
        init();
        mEnum = API_CALL.DETAILS_API_CALL;
        currentOrderApi();

        txtviewmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionSet transitionSet = new TransitionSet();
                transitionSet.addTransition(new Fade());
                transitionSet.addTransition(new ChangeBounds());
                transitionSet.setDuration(1000);
                TransitionManager.beginDelayedTransition(transitionsContainer, transitionSet);
                if (!txtviewmore.getText().toString().equalsIgnoreCase("View less")) {
                    txtviewmore.setText("View less");
                    if (lnrsipment.getChildCount() > 2) {
                        for (int i = 2; i < lnrsipment.getChildCount(); i++) {
                            lnrsipment.getChildAt(i).setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    txtviewmore.setText("View more");
                    if (lnrsipment.getChildCount() > 2) {
                        for (int i = 2; i < lnrsipment.getChildCount(); i++) {
                            lnrsipment.getChildAt(i).setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    private void init() {
        transitionsContainer = (ViewGroup) findViewById(R.id.transitions_container);
        txtorderdate = (TextView) findViewById(R.id.txtorderdate);
        txtorderno = (TextView) findViewById(R.id.txtorderno);
        txtordertotal = (TextView) findViewById(R.id.txtordertotal);
        txtshipname = (TextView) findViewById(R.id.txtshipname);
        txtphoneno1 = (TextView) findViewById(R.id.txtphoneno1);
        txtaddress1 = (TextView) findViewById(R.id.txtaddress1);
        txtshopname = (TextView) findViewById(R.id.txtshopname);
        txtshopphoneno1 = (TextView) findViewById(R.id.txtshopphoneno1);
        txtshopaddress1 = (TextView) findViewById(R.id.txtshopaddress1);
        txtitems = (TextView) findViewById(R.id.txtitems);
        txtdeliverycharge = (TextView) findViewById(R.id.txtdeliverycharge);
        txtordertotalsummery = (TextView) findViewById(R.id.txtordertotalsummery);
        lnrsipment = (LinearLayout) findViewById(R.id.lnrsipment);
        txtviewmore = (TextView) findViewById(R.id.txtviewmore);
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
        findViewById(R.id.btnReorder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnum = API_CALL.REORDER_API_CALL;
                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", sharedpreferences.getString("id", ""));
                map.put("order_id", orderId);
                CommonTask task = new CommonTask(OrderDetailActivity.this,
                        Constant.user_reorder, map, true);
                task.executeAsync();
            }
        });
    }

    private void currentOrderApi() {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", sharedpreferences.getString("id", ""));
        map.put("order_id", orderId);
        CommonTask task = new CommonTask(OrderDetailActivity.this, Constant.user_order_detail, map, true);
        task.executeAsync();
    }


    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                Log.d(TAG, "onTaskCompleted: " + new Gson().toJson(jsonObject));
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    if (mEnum == API_CALL.DETAILS_API_CALL) {
                        // DETAILS_API_CALL API CALL  RESPONSE
                        for (int k = 0; k < jsonArray.length(); k++) {
                            JSONObject jsonObjectDate = jsonArray.getJSONObject(k);
                            if (!TextUtils.isEmpty(jsonObjectDate.getString("order_date"))) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                Date newDate = null;
                                try {
                                    newDate = format.parse(jsonObjectDate.getString("order_date"));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                format = new SimpleDateFormat("MMM dd yyyy");
                                String date = format.format(newDate);
                                txtorderdate.setText(date);
                            }

                            if (!TextUtils.isEmpty(jsonObjectDate.getString("order_code"))) {
                                txtorderno.setText(jsonObjectDate.getString("order_code"));
                            }

                            if (!TextUtils.isEmpty(jsonObjectDate.getString("delivery_name"))) {
                                txtshipname.setText(jsonObjectDate.getString("delivery_name"));
                            }

                            if (!TextUtils.isEmpty(jsonObjectDate.getString("delivery_phone"))) {
                                String phoneNumber = jsonObjectDate.getString("delivery_phone");
                                // String output = phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6, 10);
                                txtphoneno1.setText("+1" + phoneNumber);
                            }
                            if (!TextUtils.isEmpty(jsonObjectDate.getString("delivery_address"))) {
                                txtaddress1.setText(jsonObjectDate.getString("delivery_address"));
                            }

                       /*     if (!TextUtils.isEmpty(jsonObjectDate.getString("store_name"))) {
                                txtshopname.setText(jsonObjectDate.getString("store_name"));
                            }

                            if (!TextUtils.isEmpty(jsonObjectDate.getString("store_phone"))) {
                                String phoneNumber = jsonObjectDate.getString("store_phone");
                                txtshopphoneno1.setText("+1" + phoneNumber);
                            }
                            if (!TextUtils.isEmpty(jsonObjectDate.getString("store_address"))) {
                                txtshopaddress1.setText(jsonObjectDate.getString("store_address"));
                            }*/

                            if (!TextUtils.isEmpty(jsonObjectDate.getString("sub_total"))) {
                                txtitems.setText("$" + numberFormat.format(Float.parseFloat(jsonObjectDate.getString("sub_total"))));
                            }

                            if (!TextUtils.isEmpty(jsonObjectDate.getString("delivery_charge"))) {
                                txtdeliverycharge.setText("$" + numberFormat.format(Float.parseFloat(jsonObjectDate.getString("delivery_charge"))));
                            }

                            if (!TextUtils.isEmpty(jsonObjectDate.getString("final_total"))) {
                                txtordertotalsummery.setText("$" + numberFormat.format(Float.parseFloat(jsonObjectDate.getString("final_total"))));
                            }

                            JSONArray jsonArrayProduct = jsonObjectDate.getJSONArray("products");
                            if (jsonArrayProduct.length() > 0) {
                                int totalProduct = jsonArrayProduct.length() - 2;
                                if (totalProduct <= 0) {
                                    txtviewmore.setVisibility(View.GONE);
                                } else {
                                    txtviewmore.setText("View more");
                                }
                                for (int i = 0; i < jsonArrayProduct.length(); i++) {
                                    JSONObject jsonObjectProd = jsonArrayProduct.getJSONObject(i);
                                    String imgUrl = "";
                                    String prodName = "";
                                    String desc = "";
                                    String type = "";
                                    String price = "";
                                    String color = "";
                                    String quantity = "";
                                    if (!TextUtils.isEmpty(jsonObjectProd.getString("product_name"))) {
                                        prodName = jsonObjectProd.getString("product_name");
                                    }
                                    if (!TextUtils.isEmpty(jsonObjectProd.getString("attribute_description"))) {
                                        desc = jsonObjectProd.getString("attribute_description");
                                    }
                                    if (!TextUtils.isEmpty(jsonObjectProd.getString("type"))) {
                                        type = jsonObjectProd.getString("type");
                                    }
                                    if (!TextUtils.isEmpty(jsonObjectProd.getString("price"))) {
                                        price = jsonObjectProd.getString("price");
                                    }
                                    if (!TextUtils.isEmpty(jsonObjectProd.getString("color"))) {
                                        color = jsonObjectProd.getString("color");
                                    }
                                    if (!TextUtils.isEmpty(jsonObjectProd.getString("image_url"))) {
                                        imgUrl = jsonObjectProd.getString("image_url");
                                    }

                                    if (!TextUtils.isEmpty(jsonObjectProd.getString("quantity"))) {
                                        quantity = jsonObjectProd.getString("quantity");
                                    }
                                    addProduct(i, imgUrl, prodName, desc, type, price, color, quantity);
                                }
                                if (lnrsipment.getChildCount() > 2) {
                                    for (int i = 2; i < lnrsipment.getChildCount(); i++) {
                                        lnrsipment.getChildAt(i).setVisibility(View.GONE);
                                    }
                                }
                            }
                            if (!TextUtils.isEmpty(jsonObjectDate.getString("final_total"))) {
                                txtordertotal.setText("$" + numberFormat.format(Float.parseFloat(jsonObjectDate.getString("final_total"))) + " (" + jsonArrayProduct.length() + " Items)");
                            }
                        }
                    } else {
                        //Handle REORDER_API_CALL response
                        openOrderDrawer();
                    }
                } else {
                    //Handle REORDER_API_CALL response error
                    if (mEnum == API_CALL.REORDER_API_CALL) {
                        if (msg.startsWith(getResources().getString(R.string.cart_error)))
                            CommonUtils.openDialogWithYesNoButton(this, msg, getResources()
                                            .getString(R.string.go_to_cart), getResources().getString(R.string.cancel), new YesNoAlertDialogInterface() {
                                        @Override
                                        public void onPositiveButtonClicked() {
                                            openOrderDrawer();
                                        }

                                        @Override
                                        public void onNegativeButtonClicked() {

                                        }
                                    }
                            );
                        else
                            showToast(msg);
                    } else {
                        //Handle DETAILS_API_CALL response error
                        showToast(msg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addProduct(int pos, String imgUrl, String prodName, String desc, String type, String price, String color, String quantity) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_orderdetail_raw, lnrsipment, false);
        final CircleImageView imgProduct = (CircleImageView) view.findViewById(R.id.imgProduct);
        TextView txtpname = (TextView) view.findViewById(R.id.txtpname);
        TextView txtdesc = (TextView) view.findViewById(R.id.txtdesc);
        TextView txttype = (TextView) view.findViewById(R.id.txttype);
        TextView txtqntity = (TextView) view.findViewById(R.id.txtqntity);
        TextView txtproductprice = (TextView) view.findViewById(R.id.txtproductprice);
        View viewDivider = view.findViewById(R.id.viewDivider);

        txtpname.setText(prodName);
        txtdesc.setText(desc);
        txttype.setText(type);
        txtqntity.setText("Quantity: " + quantity);

        float pr = Float.parseFloat(price);
        float qty = Float.parseFloat(quantity);
        float finalPrice = pr * qty;
        txtproductprice.setText("$" + numberFormat.format(finalPrice));

        SimpleTarget targetStore = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                // do something with the bitmap
                // for demonstration purposes, let's just set it to an ImageView
                imgProduct.setImageBitmap(bitmap);
            }
        };
        if (!TextUtils.isEmpty(imgUrl)) {
            Glide.with(OrderDetailActivity.this).load(imgUrl).asBitmap().placeholder(R.drawable.logo).into(targetStore);
        }

        GradientDrawable bgShape = (GradientDrawable) txttype.getBackground();
        bgShape.setColor(Color.parseColor(color));
        lnrsipment.addView(view);

        if (pos == 0) {
            viewDivider.setVisibility(View.GONE);
        } else {
            viewDivider.setVisibility(View.VISIBLE);
        }
    }

    public void onBack(View view) {
        finish();
    }

    /**
     * Open Order Drawer
     */
    public void openOrderDrawer() {
        // RE ORDER API CALL  RESPONSE
        //Close current activity and re - deliver result in On Activity Result
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constant.OPEN_DRAWER_DATA_KEY, true);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
