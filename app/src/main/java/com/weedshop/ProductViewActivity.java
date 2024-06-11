package com.weedshop;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.darwindeveloper.wcviewpager.WCViewPagerIndicator;
import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.weedshop.Adapter.SlidingImage_Adapter;
import com.weedshop.model.Product;
import com.weedshop.model.ProductDetail;
import com.weedshop.utils.Constant;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductViewActivity extends SideMenuActivity implements OnTaskCompleted {
    private static final int PRODUCT_DETAIL_REQUEST_CODE = 3001;
    private static final int ADD_CART_REQUEST_CODE = 3002;
    private TextView txtBack;
    private ImageView ivSelectedDot, ivBack;
    private LinearLayout llSelectedFilter;
    private TextView tvSelectedWeight, tvSelectedFilter, tvCountQuantity, txtDesc, txtAvailable;
    private Button btn_add_cart;
    private int qtyCount = 1, maxQty = 0;
    private ImageView rlPlusQuantity, rlMinusQuantity;
    private ProductDetail productDetail;
    private WCViewPagerIndicator wcViewPagerIndicator;
    private HorizontalInfiniteCycleViewPager infiniteCycleViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpMenu(this, R.layout.activity_product_view);

        llSelectedFilter = (LinearLayout) findViewById(R.id.ll_selected_filter);
        tvSelectedWeight = (TextView) findViewById(R.id.tv_selected_weight);
        tvSelectedFilter = (TextView) findViewById(R.id.tv_selected_filter);
        txtAvailable = (TextView) findViewById(R.id.txtAvailable);
        txtDesc = (TextView) findViewById(R.id.txtDesc);
        txtDesc.setMovementMethod(new ScrollingMovementMethod());
        ivSelectedDot = (ImageView) findViewById(R.id.iv_selected_dot);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        tvCountQuantity = (TextView) findViewById(R.id.tv_count_quantity);

        tvCountQuantity.setText("1");
        rlMinusQuantity = (ImageView) findViewById(R.id.rl_minus_quantity_pd);
        rlPlusQuantity = (ImageView) findViewById(R.id.rl_plus_quantity_pd);
        txtBack = (TextView) findViewById(R.id.txtBack);
        wcViewPagerIndicator = (WCViewPagerIndicator) findViewById(R.id.wcviewpager);
        infiniteCycleViewPager = (HorizontalInfiniteCycleViewPager) findViewById(R.id.hicvp);
        btn_add_cart = (Button) findViewById(R.id.btn_add_cart);
        tvCountQuantity.setBackgroundResource(R.drawable.round_corner);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Product product = (Product) bundle.getSerializable("data");
            if (product != null) {
                HashMap<String, String> map = new HashMap<>();
                map.put("store_id", product.storeId);
                map.put("product_id", product.id);
                CommonTask task = new CommonTask(ProductViewActivity.this, Constant.product_Detail, map, PRODUCT_DETAIL_REQUEST_CODE, true);
                task.executeAsync();
            }
        }

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_add_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedpreferences.getString("id", "") != null && !sharedpreferences.getString("id", "").isEmpty()) {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("user_id", sharedpreferences.getString(Pref.id, ""));
                    params.put("store_id", productDetail.storeId);
                    params.put("product_id", productDetail.id);
                    params.put("quantity", tvCountQuantity.getText().toString());
                    params.put("action", "add");

                    CommonTask handler = new CommonTask(ProductViewActivity.this, Constant.cart_api, params, ADD_CART_REQUEST_CODE, true);
                    handler.executeAsync();
                }else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            ProductViewActivity.this);
                    // set dialog message
                    alertDialogBuilder
                            .setMessage(R.string.str_login_required)
                            .setCancelable(false)
                            .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    startActivity(new Intent(ProductViewActivity.this, SignInActivity.class));
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, just close
                                    // the dialog box and do nothing
                                    dialog.dismiss();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
            }
        });

        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        rlMinusQuantity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (qtyCount > 1) {
                    qtyCount--;
                    tvCountQuantity.setText(String.valueOf(qtyCount));
                } else {
                    tvCountQuantity.setText("1");
                }
                qtyCount = Integer.parseInt(tvCountQuantity.getText().toString());
            }
        });
        rlPlusQuantity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (qtyCount < maxQty) {
                    qtyCount++;
                    tvCountQuantity.setText(String.valueOf(qtyCount));
                    qtyCount = Integer.parseInt(tvCountQuantity.getText().toString());
                }
            }
        });
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (TextUtils.isEmpty(result))
            return;

        if (PRODUCT_DETAIL_REQUEST_CODE == requestCode) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {
                    final ProgressDialog dialog;
                    dialog = new ProgressDialog(ProductViewActivity.this);
                    dialog.setMessage("Please wait");
                    dialog.setCancelable(false);
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    }, 7000);

                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        productDetail = new ProductDetail();
                        productDetail.id = object.getString("id");
                        productDetail.name = object.getString("name");
                        productDetail.storeId = object.getString("store_id");
                        productDetail.typeId = object.getString("type_id");
                        productDetail.image = object.getString("main_image_url");
                        productDetail.price = object.getString("price");
                        productDetail.quantity = object.getString("quantity");
                        maxQty = Integer.parseInt(productDetail.quantity);
                        if (maxQty <= 0) {
                            tvCountQuantity.setText("0");
                            txtAvailable.setText("Sold Out.");
                        } else {
                            txtAvailable.setText("Available Quantity: " + productDetail.quantity);
                        }
                        productDetail.description = object.getString("description");
                        productDetail.delivery_charge = object.getString("delivery_charge");
                        productDetail.color = object.getString("color");
                        productDetail.type = object.getString("type");
                        productDetail.main_image_url = object.getString("main_image_url");

                        if (!TextUtils.isEmpty(object.getString("attributes"))) {
                            List<ProductDetail.Attribute> attribList = new ArrayList<>();
                            JSONArray arrayAttrib = object.getJSONArray("attributes");
                            for (int j = 0; j < arrayAttrib.length(); j++) {
                                JSONObject attrib = arrayAttrib.getJSONObject(j);
                                ProductDetail.Attribute attribute = new ProductDetail.Attribute();
                                attribute.attributeId = attrib.getString("attribute_id");
                                attribute.attributeText = attrib.getString("attribute_text");
                                attribute.name = attrib.getString("name");
                                attribList.add(attribute);
                            }
                            productDetail.attributes = attribList;
                        }

                        if (!TextUtils.isEmpty(object.getString("images"))) {
                            List<ProductDetail.Images> imagesList = new ArrayList<>();
                            JSONArray arrayAttrib = object.getJSONArray("images");
                            if(!TextUtils.isEmpty(productDetail.main_image_url)){
                                ProductDetail.Images images = new ProductDetail.Images();
                                images.image = "";
                                images.image_url = productDetail.main_image_url;
                                images.product_id = "";
                                imagesList.add(images);
                            }
                            for (int j = 0; j < arrayAttrib.length(); j++) {
                                JSONObject attrib = arrayAttrib.getJSONObject(j);
                                ProductDetail.Images images = new ProductDetail.Images();
                                images.image = attrib.getString("image");
                                images.image_url = attrib.getString("image_url");
                                images.product_id = attrib.getString("product_id");
                                imagesList.add(images);
                            }
                            productDetail.images = imagesList;
                        }
                    }
                    setData();
                } else {
                    showToast(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ADD_CART_REQUEST_CODE == requestCode) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                boolean response = jsonObject.getBoolean("response");

                showToast(msg);

                if (response) {
                    Constant.cartCount = Constant.cartCount + 1;
                    txtCounter.setText(String.valueOf(Constant.cartCount));
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setData() {
        GradientDrawable bgShape1 = (GradientDrawable) ivSelectedDot.getBackground();
        bgShape1.setColor(Color.WHITE);

        GradientDrawable bgShape = (GradientDrawable) llSelectedFilter.getBackground();
        bgShape.setColor(Color.parseColor(productDetail.color));

        tvSelectedFilter.setText(productDetail.type.toUpperCase());
        txtDesc.setText(productDetail.description);

        tvCountQuantity.setBackgroundResource(R.drawable.round_corner);
        tvSelectedWeight.setText("");
        if (productDetail.attributes != null && productDetail.attributes.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (ProductDetail.Attribute attribute : productDetail.attributes) {
                if (builder.length() == 0) {
                    builder.append(attribute.attributeText);
                    builder.append(" ");
                    builder.append(attribute.name);
                } else {
                    builder.append(" | ");
                    builder.append(attribute.attributeText);
                    builder.append(" ");
                    builder.append(attribute.name);
                }
            }
            tvSelectedWeight.setText(builder.toString());
        }
        txt_screen_title.setText(productDetail.name);

        SlidingImage_Adapter slidingImage_adapter = new SlidingImage_Adapter(ProductViewActivity.this, productDetail.images);
        infiniteCycleViewPager.setAdapter(slidingImage_adapter);
        infiniteCycleViewPager.setScrollDuration(500);
        infiniteCycleViewPager.setInterpolator(null);
        infiniteCycleViewPager.setMediumScaled(true);
        infiniteCycleViewPager.setMaxPageScale(0.84f);
        infiniteCycleViewPager.setMinPageScale(0.8F);
        infiniteCycleViewPager.setCenterPageScaleOffset(30.0F);
        infiniteCycleViewPager.setMinPageScaleOffset(30.0F);

        wcViewPagerIndicator.setViewPager(infiniteCycleViewPager);

        infiniteCycleViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                wcViewPagerIndicator.setSelectedindicator(infiniteCycleViewPager.getRealItem());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
