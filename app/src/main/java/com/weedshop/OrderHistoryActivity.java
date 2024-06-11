package com.weedshop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.weedshop.Adapter.OrderHistoryAdapter;
import com.weedshop.model.Order;
import com.weedshop.utils.ClickListener;
import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Constant;
import com.weedshop.utils.EndlessRecyclerViewScrollListener;
import com.weedshop.utils.NotificationUtils;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.RecyclerTouchListener;
import com.weedshop.webservices.CommonTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class OrderHistoryActivity extends SideMenuActivity implements OnTaskCompleted {
    private LinearLayout llSearchView;
    private ImageView ivFilter;
    private Button btn_search;
    private EditText edtSearch;
    private TextView txtBack;
    private ImageView ivBack;
    private RecyclerView rvHistory;

    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    private RadioGroup radioGroup;
    private String filterDate = "", filterText = "";
    private int totalPages = 0;
    private TextView txtEmptyOrder;
    private OrderHistoryAdapter adapter;
    private ArrayList<Order> orderList;
    private String lastDate = null;
    private Boolean isSearch = false;
    private LinearLayoutManager layoutManager;

    public static final int REQ_CODE = 001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpMenu(this, R.layout.activity_order_history);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        txt_screen_title.setText("Order History");
        llSearchView = (LinearLayout) findViewById(R.id.llSearchView);
        btn_search = (Button) findViewById(R.id.btn_search);
        ivFilter = (ImageView) findViewById(R.id.ivFilter);
        edtSearch = (EditText) findViewById(R.id.edtSearch);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        txtBack = (TextView) findViewById(R.id.txtBack);
        rvHistory = (RecyclerView) findViewById(R.id.rvHistory);

        txtEmptyOrder = (TextView) findViewById(R.id.txtEmptyOrder);
        //  rvHistory.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(OrderHistoryActivity.this);
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setItemAnimator(new DefaultItemAnimator());
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

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

        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.hideKeyboard(OrderHistoryActivity.this);
                if (llSearchView.getVisibility() == View.VISIBLE) {
                    slideToTop(llSearchView);
                    llSearchView.setVisibility(View.GONE);
                } else {
                    slideToBottom(llSearchView);
                    llSearchView.setVisibility(View.VISIBLE);
                }
                edtSearch.setText("");
            }
        });

        orderHistoryApi(1, false);

        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                page = page + 1;
                orderHistoryApi(page, false);
            }
        };

        rvHistory.addOnScrollListener(endlessRecyclerViewScrollListener);

        rvHistory.addOnItemTouchListener(new RecyclerTouchListener(this, rvHistory, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (llSearchView.getVisibility() == View.GONE) {
                    Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
                    intent.putExtra("orderId", adapter.getList().get(position).id);
                    startActivityForResult(intent, REQ_CODE);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    CommonUtils.hideKeyboard(OrderHistoryActivity.this);
                    String search = edtSearch.getText().toString().trim();
                    if (!TextUtils.isEmpty(search)) btn_search.callOnClick();
                }
                return false;
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search = edtSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(search)) {
                    filterText = search;
                } else {
                    filterText = "";
                }
                int id = radioGroup.getCheckedRadioButtonId();
                if (id > 0) {
                    filterDate = radioGroup.findViewById(id).getTag().toString();
                }
                CommonUtils.hideKeyboard(OrderHistoryActivity.this);
                if (llSearchView.getVisibility() == View.VISIBLE) {
                    slideToTop(llSearchView);
                    llSearchView.setVisibility(View.GONE);
                }
                endlessRecyclerViewScrollListener.resetState();
                orderHistoryApi(1, true);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    private void orderHistoryApi(int page, boolean isSearch) {
        this.isSearch = isSearch;
        if (totalPages > 0) {
            if (page > totalPages && page != totalPages) {
                return;
            }
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", sharedpreferences.getString("id", ""));
        map.put("page", page + "");
        map.put("filter_text", filterText);
        map.put("filter_date", filterDate);
        CommonTask task = new CommonTask(OrderHistoryActivity.this, Constant.user_order_history, map, true);
        task.executeAsync();
    }

    private void slideToTop(View view) {
        //view.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(this,
                R.anim.slide_up);
        view.startAnimation(animation);
    }

    private void slideToBottom(View view) {
        //view.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this,
                R.anim.slide_bottom);
        view.startAnimation(animation);
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (TextUtils.isEmpty(result)) {
            txtEmptyOrder.setVisibility(View.VISIBLE);
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            String msg = jsonObject.getString("msg");
            boolean response = jsonObject.getBoolean("response");
            if (response) {
                txtEmptyOrder.setVisibility(View.GONE);
                totalPages = jsonObject.getInt("totalPages");
                orderList = new ArrayList<>();
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Order order = new Order();
                    order.id = object.getString("id");
                    order.orderCode = object.getString("order_code");
                    order.storeId = object.getString("store_id");
                    order.userId = object.getString("user_id");
                    order.driverId = object.getString("driver_id");
                    if (!TextUtils.isEmpty(object.getString("order_date"))) {
                        order.orderDate = object.getString("order_date");
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Date newDate = null;
                        try {
                            newDate = format.parse(order.orderDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        format = new SimpleDateFormat("MMM dd yyyy");
                        String date = format.format(newDate);
                        if (lastDate == null) {
                            lastDate = date;
                            order.orderDate = lastDate;
                        } else {
                            if (lastDate.equalsIgnoreCase(date)) {
                                order.orderDate = "";
                            } else {
                                lastDate = date;
                                order.orderDate = lastDate;
                            }
                        }
                    }
                    order.finalTotal = object.getString("final_total");
                    order.status = object.getString("status");
                    order.totalProducts = object.getString("total_products");

                    if (!TextUtils.isEmpty(object.getString("products"))) {
                        ArrayList<Order.Product> productList = new ArrayList<>();
                        JSONArray arrayAttrib = object.getJSONArray("products");
                        for (int j = 0; j < arrayAttrib.length(); j++) {
                            JSONObject attrib = arrayAttrib.getJSONObject(j);
                            Order.Product product = new Order.Product();
                            product.orderId = attrib.getString("order_id");
                            product.productId = attrib.getString("product_id");
                            product.productName = attrib.getString("product_name");
                            product.price = attrib.getString("price");
                            product.quantity = attrib.getString("quantity");
                            product.attributeDescription = attrib.getString("attribute_description");
                            product.imageUrl = attrib.getString("image_url");
                            product.type = attrib.getString("type");
                            product.color = attrib.getString("color");
                            productList.add(product);
                        }
                        order.products = productList;
                    }
                    orderList.add(order);
                }
                if (isSearch) {
                    adapter = null;
                    rvHistory.setAdapter(adapter);
                }
                if (adapter == null) {
                    adapter = new OrderHistoryAdapter(OrderHistoryActivity.this, orderList);
                    rvHistory.setAdapter(adapter);
                } else {
                    adapter.updateList(orderList);
                }
            } else {
                showToast(msg);
                // if (adapter == null || adapter.getList().size() <= 0) {
                totalPages = 0;
                adapter = null;
                endlessRecyclerViewScrollListener.resetState();
                rvHistory.setAdapter(adapter);
                txtEmptyOrder.setVisibility(View.VISIBLE);
                //  }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check that it is the Order Details Activity with an OK result
        if (requestCode == REQ_CODE) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK

                // get boolean data from Intent
                boolean returnData = data.getBooleanExtra(Constant.OPEN_DRAWER_DATA_KEY, false);

                if (returnData) {
                    //open cart
                    mMenuDrawerRight.openMenu();
                }

            }
        }
    }
}
