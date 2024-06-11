package com.weedshop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.weedshop.model.DataModel;
import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Constant;
import com.weedshop.utils.EndlessRecyclerOnScrollListener;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MTPC-110 on 5/24/2017.
 */

public class CurrentOrderStatusActivity extends SideMenuActivity implements OnTaskCompleted {
    private static final int CURRENT_ORDER_STATUS_REQUEST_CODE = 7897;

    RecyclerView rvOrderStatus;
    List<DataModel> allSampleData = new ArrayList<DataModel>();
    CurrentOrderAdapter adapter;
    int pageNumber = 1;
    String dateOld = "";
    boolean isFromLoadMore = false;
    LinearLayoutManager manager;
    private TextView txtEmptyOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpMenu(this, R.layout.activity_current_order_status);

        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        rvOrderStatus = (RecyclerView) findViewById(R.id.rvOrderStatus);
        txtEmptyOrder = (TextView) findViewById(R.id.txtEmptyOrder);
        //set title
        txt_screen_title.setText(getResources().getString(R.string.title_current_order_status));

        manager = new LinearLayoutManager(CurrentOrderStatusActivity.this, LinearLayoutManager.VERTICAL, false);
        rvOrderStatus.setHasFixedSize(true);
        rvOrderStatus.setLayoutManager(manager);
        rvOrderStatus.setItemAnimator(new DefaultItemAnimator());

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

        rvOrderStatus.setOnScrollListener(new EndlessRecyclerOnScrollListener(manager) {
            @Override
            public void onLoadMore(int current_page) {
                if (allSampleData != null && allSampleData.size() >= 10) {
                    pageNumber++;
                    isFromLoadMore = true;
                    callCurrentOrderApi(pageNumber, true);
                }
            }
        });
        if (CommonUtils.isNetworkConnected(CurrentOrderStatusActivity.this)) {
            callCurrentOrderApi(pageNumber, false);
        }
    }

    private void callCurrentOrderApi(int pageVal, boolean isFromLoadMore) {
        CommonTask task = null;
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", sharedpreferences.getString(Pref.id, ""));
        map.put("page", "" + pageVal);
        if (isFromLoadMore) {
            task = new CommonTask(CurrentOrderStatusActivity.this, Constant.current_order_status_api, map, CURRENT_ORDER_STATUS_REQUEST_CODE, false);
        } else {
            task = new CommonTask(CurrentOrderStatusActivity.this, Constant.current_order_status_api, map, CURRENT_ORDER_STATUS_REQUEST_CODE, true);
        }
        task.executeAsync();
    }

    private void populateSampleData() {
        if (allSampleData.size() > 0) {
            adapter = new CurrentOrderAdapter(allSampleData);
            //adapter.setLayoutManager(manager);
            if (!isFromLoadMore) {
                rvOrderStatus.setAdapter(adapter);
            } else {
                //from load more..just notify adapter
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (TextUtils.isEmpty(result)) {
            txtEmptyOrder.setVisibility(View.VISIBLE);
            return;
        }
        if (requestCode == CURRENT_ORDER_STATUS_REQUEST_CODE) {
            try {
                JSONArray jsonArrayData = null;
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                if (jsonObject.getString("response") != null && jsonObject.getString("response").equalsIgnoreCase("true")) {
                    txtEmptyOrder.setVisibility(View.GONE);
                    jsonArrayData = jsonObject.getJSONArray("data");
                    if (jsonArrayData != null
                            && jsonArrayData.length() > 0
                            ) {
                        if (!isFromLoadMore) {
                            allSampleData.clear();
                        }
                        for (int i = 0; i < jsonArrayData.length(); i++) {
                            DataModel dm = new DataModel();
                            JSONObject jsonObject1 = jsonArrayData.getJSONObject(i);
                            String dateUpdated = jsonObject1.getString("order_date");
                            if (!TextUtils.isEmpty(dateUpdated)) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                Date newDate = null;
                                try {
                                    newDate = format.parse(dateUpdated);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                format = new SimpleDateFormat("MMM dd yyyy");
                                String dateNew = format.format(newDate);
                                JSONArray jsonArrayProducts = jsonObject1.getJSONArray("products");
                                if (jsonArrayProducts != null && jsonArrayProducts.length() > 0) {
                                    ArrayList<HashMap<String, String>> singleItem = new ArrayList<>();
                                    for (int j = 0; j < jsonArrayProducts.length(); j++) {
                                        if (j == 0) {
                                            //only get first item array
                                            JSONObject jsonObject2 = jsonArrayProducts.getJSONObject(j);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("order_code", jsonObject1.getString("order_code"));
                                            hashMap.put("product_name", jsonObject2.getString("product_name"));
                                            hashMap.put("product_id", jsonObject2.getString("product_id"));
                                            hashMap.put("order_id", jsonObject2.getString("order_id"));
                                            hashMap.put("price", jsonObject2.getString("price"));
                                            hashMap.put("quantity", jsonObject2.getString("quantity"));
                                            hashMap.put("attribute_description", jsonObject2.getString("attribute_description"));
                                            hashMap.put("image_url", jsonObject2.getString("image_url"));
                                            hashMap.put("type", jsonObject2.getString("type"));
                                            hashMap.put("color", jsonObject2.getString("color"));
                                            hashMap.put("more_product", "" + jsonArrayProducts.length());
                                            if (dateOld.compareTo(dateNew) == 0) {
                                                //it's matched..dont
                                                hashMap.put("is_header", "false");
                                                hashMap.put("order_date", "");
                                            } else {
                                                dateOld = dateNew;
                                                hashMap.put("is_header", "true");
                                                hashMap.put("order_date", dateOld);
                                            }
                                            singleItem.add(hashMap);
                                        }
                                    }
                                    dm.setAllItemsInSection(singleItem);
                                }
                            }
                            allSampleData.add(dm);
                        }
                        populateSampleData();
                    }
                } else {
                    showToast(msg);
                    if (adapter == null || adapter.getList().size() <= 0) {
                        pageNumber = 1;
                        adapter = null;
                        rvOrderStatus.setAdapter(adapter);
                        txtEmptyOrder.setVisibility(View.VISIBLE);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class CurrentOrderAdapter extends RecyclerView.Adapter<CurrentOrderAdapter.MyViewHolder> {
        private List<DataModel> allData;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            final TextView itemTitle;
            final TextView itemDescription;
            final TextView itemType;
            final TextView tvPrice;
            final TextView tvDate;
            final TextView tvMoreProduct;
            ImageView imgProduct;
            ImageView imgcoupon;
            LinearLayout linearParent;

            public MyViewHolder(View itemView) {
                super(itemView);
                itemTitle = (TextView) itemView.findViewById(R.id.itemTitle);
                itemType = (TextView) itemView.findViewById(R.id.itemType);
                tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
                tvDate = (TextView) itemView.findViewById(R.id.tvDate);
                tvMoreProduct = (TextView) itemView.findViewById(R.id.tvMoreProduct);
                itemDescription = (TextView) itemView.findViewById(R.id.itemDescription);
                imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
                imgcoupon = (ImageView) itemView.findViewById(R.id.imgcoupon);
                linearParent = (LinearLayout) itemView.findViewById(R.id.linearParent);
            }
        }

        public List<DataModel> getList() {
            return allData;
        }

        public CurrentOrderAdapter(List<DataModel> allData) {
            this.allData = allData;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder itemViewHolder, int position) {
            List<HashMap<String, String>> itemsInSection = allData.get(position).getAllItemsInSection();
            if (itemsInSection.size() > 0) {
                for (int i = 0; i < itemsInSection.size(); i++) {
                    if (i == 0) {

                        final String productName = itemsInSection.get(i).get("product_name");
                        String productDesc = itemsInSection.get(i).get("attribute_description");
                        String productPrice = itemsInSection.get(i).get("price");
                        String productquantity = itemsInSection.get(i).get("quantity");
                        String productImage = itemsInSection.get(i).get("image_url");
                        String productType = itemsInSection.get(i).get("type");
                        String productColor = itemsInSection.get(i).get("color");
                        String isHeaders = itemsInSection.get(i).get("is_header");
                        String order_date = itemsInSection.get(i).get("order_date");
                        String moreProduct = itemsInSection.get(i).get("more_product");
                        String productId = itemsInSection.get(i).get("product_id");
                        final String orderCode = itemsInSection.get(i).get("order_code");
                        final String orderId = itemsInSection.get(i).get("order_id");

                        if (productImage != null &&
                                !TextUtils.isEmpty(productImage)) {
                            CommonUtils.setImageGlideProductDetails(CurrentOrderStatusActivity.this, productImage
                                    , itemViewHolder.imgProduct);
                        }

                        if (moreProduct != null &&
                                !TextUtils.isEmpty(moreProduct)
                                && !moreProduct.equalsIgnoreCase("0")
                                && !moreProduct.equalsIgnoreCase("1")
                                ) {
                            itemViewHolder.tvMoreProduct.setText("+" + String.valueOf(Integer.parseInt(moreProduct) - 1) + " more");
                        }

                        if (isHeaders != null &&
                                !TextUtils.isEmpty(isHeaders)) {
                            if (isHeaders.equalsIgnoreCase("true")) {
                                //show date ..set date
                                itemViewHolder.tvDate.setVisibility(View.VISIBLE);
                                itemViewHolder.tvDate.setText("Date: " + order_date);
                            } else {
                                //gone date text
                                itemViewHolder.tvDate.setVisibility(View.GONE);
                            }
                        }

                        if (productName != null &&
                                !TextUtils.isEmpty(productName)) {
                            itemViewHolder.itemTitle.setText(productName);
                        }

                        if (productDesc != null &&
                                !TextUtils.isEmpty(productDesc)) {
                            itemViewHolder.itemDescription.setText(productDesc);
                        }

                        if (productColor != null &&
                                !TextUtils.isEmpty(productColor)) {
                            GradientDrawable bgShape = (GradientDrawable) itemViewHolder.itemType.getBackground();
                            bgShape.setColor(Color.parseColor(productColor));
                        }

                        if (productType != null &&
                                !TextUtils.isEmpty(productType)) {

                            itemViewHolder.itemType.setText(productType);
                        }
                        if (productPrice != null &&
                                !TextUtils.isEmpty(productPrice)) {

                            itemViewHolder.tvPrice.setText("$" + productPrice);
                        }

                        itemViewHolder.linearParent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                callToCheckIsDriverAssignedApi(orderId, productName, orderCode);
                            }
                        });
                    }
                }

            }

        }

        @Override
        public int getItemCount() {
            return allData.size();
        }
    }

    private void callToCheckIsDriverAssignedApi(final String orderId, final String productName,final String orderCode) {
        CommonTask task = null;
        HashMap<String, String> map = new HashMap<>();
        map.put("order_id", orderId);
        task = new CommonTask(CurrentOrderStatusActivity.this, Constant.is_driver_assigned_status_api, map, new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result, int requestCode) {
                System.out.println("Results:" + result);
                //{"response":"false","msg":"Driver not assigned yet!","data":{"status":"Pending"}}
                try {
                    if (TextUtils.isEmpty(result)) return;
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                    String status = jsonObject1.getString("status");
                    String msg = jsonObject.getString("msg");
                    if (status != null &&
                            !TextUtils.isEmpty(status)
                            && status.equalsIgnoreCase("Inprocess")
                            ) {
                        //go ahead..driver assigned
                        Intent i = new Intent(CurrentOrderStatusActivity.this, MapActivity.class);
                        i.putExtra("order_id", orderId);
                        i.putExtra("productName", productName);
                        i.putExtra("orderCode", orderCode);

                        startActivity(i);
                    } else {
                        showToast(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, true);
        task.executeAsync();
    }


}
