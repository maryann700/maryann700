package com.weedshop;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.weedshop.Adapter.OrderConfirmAdapter;
import com.weedshop.model.Address;
import com.weedshop.model.Cart;
import com.weedshop.model.DeliveryCharge;
import com.weedshop.utils.Constant;
import com.weedshop.utils.JsonUtils;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import net.simonvt.menudrawer.MenuDrawer;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderConfirmationActivity extends SideMenuActivity implements OnTaskCompleted {
    private static final int GET_CART_ITEM_REQUEST_CODE = 6001;
    private static final int ORDER_REQUEST_CODE = 6002;
    private RecyclerView recyclerView;
    private OrderConfirmAdapter adapter;
    private ArrayList<Cart> cartList;

    private TextView tv_delivery_charge, tv_sub_total, txtName, txtPhone, txtAddress;
    private Button btn_order;
    private Address address;
    private String deliveryCharge = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpMenu(this, R.layout.activity_order_conformation);
        mMenuDrawerRight.setTouchMode(MenuDrawer.TOUCH_MODE_NONE);
        rlCart.setVisibility(View.INVISIBLE);

        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        txt_screen_title.setText("Order Confirmation");
        recyclerView = (RecyclerView) findViewById(R.id.rvOrders);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btn_order = (Button) findViewById(R.id.btn_order);
        tv_delivery_charge = (TextView) findViewById(R.id.tv_delivery_charge);
        tv_sub_total = (TextView) findViewById(R.id.tv_sub_total);
        txtName = (TextView) findViewById(R.id.txtName);
        txtPhone = (TextView) findViewById(R.id.txtPhone);
        txtAddress = (TextView) findViewById(R.id.txtAddress);

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("user_id", sharedpreferences.getString(Pref.id, ""));
        params.put("action", "list");
        CommonTask handler = new CommonTask(OrderConfirmationActivity.this, Constant.cart_api, params, GET_CART_ITEM_REQUEST_CODE, true);
        handler.executeAsync();

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
        address = (Address) getIntent().getSerializableExtra("address");

        if (address != null) {
            txtName.setText(address.firstname + " " + address.lastname);
            txtPhone.setText("+1" + address.phone);

            StringBuilder builder = new StringBuilder();
            builder.append(address.address);
            builder.append(", ");
            builder.append(address.city);
            builder.append(", ");
            builder.append(address.region);
            builder.append(", ");
            builder.append(address.state);
            builder.append(" ");
            builder.append(address.zipcode);
            txtAddress.setText(builder.toString());
        }

        btn_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartList == null || cartList.size() <= 0) return;
                String jsonArray = JsonUtils.getJsonArrayString(getProductList(cartList));
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("user_id", sharedpreferences.getString("id", ""));
                params.put("store_id", cartList.get(0).storeId);
                params.put("address", address.id);
                if (TextUtils.isEmpty(deliveryCharge))
                    deliveryCharge = String.valueOf(getDeliveryCharge());
                params.put("delivery_charge", deliveryCharge);
                params.put("product", jsonArray);

                CommonTask task = new CommonTask(OrderConfirmationActivity.this, Constant.order, params, ORDER_REQUEST_CODE, true);
                task.executeAsync();
            }
        });
    }

    private ArrayList<Map<String, Object>> getProductList(ArrayList<Cart> cartList) {
        ArrayList<Map<String, Object>> itemList = new ArrayList<>();
        for (int i = 0; i < cartList.size(); i++) {
            Map<String, String> hashMap = new HashMap<>();
            Map<String, Object> map = new HashMap<>();

            hashMap.put("quantity", cartList.get(i).cartQuantity);
            hashMap.put("product_id", cartList.get(i).productId);
            StringBuilder builder = new StringBuilder();
            if (cartList.get(i).attributes != null && cartList.get(i).attributes.size() > 0) {
                for (Cart.Attribute attribute : cartList.get(i).attributes) {
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
            }
            hashMap.put("attribute_description", builder.toString());
            hashMap.put("price", cartList.get(i).price);
            hashMap.put("product_name", cartList.get(i).name);
            hashMap.put("type", cartList.get(i).type);

            ObjectMapper m = new ObjectMapper();
            map = m.convertValue(hashMap, Map.class);
            itemList.add(map);
        }
        return itemList;
    }

    private void parseResponse(String result) {
        try {
            cartList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(result);
            String msg = jsonObject.getString("msg");
            boolean response = jsonObject.getBoolean("response");
            if (response) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Cart cart = new Cart();
                    cart.storeId = object.getString("store_id");
                    cart.storeName = object.getString("store_name");
                    cart.productId = object.getString("product_id");
                    cart.cartQuantity = object.getString("cart_quantity");
                    cart.totalQuantity = object.getString("total_quantity");
                    cart.name = object.getString("name");
                    cart.type = object.getString("type");
                    cart.color = object.getString("color");
                    cart.price = object.getString("price");
                    cart.image = object.getString("image");
                    cart.latitude = object.getString("latitude");
                    cart.longitude = object.getString("longitude");
                    cart.imageUrl = object.getString("image_url");

                    if (!TextUtils.isEmpty(object.getString("attributes"))) {
                        List<Cart.Attribute> attribList = new ArrayList<>();
                        JSONArray arrayAttrib = object.getJSONArray("attributes");
                        for (int j = 0; j < arrayAttrib.length(); j++) {
                            JSONObject attrib = arrayAttrib.getJSONObject(j);
                            Cart.Attribute attribute = new Cart.Attribute();
                            attribute.attributeId = attrib.getString("attribute_id");
                            attribute.attributeText = attrib.getString("attribute_text");
                            attribute.name = attrib.getString("name");
                            attribList.add(attribute);
                        }
                        cart.attributes = attribList;
                    }
                    cartList.add(cart);
                }
                if (cartList == null | cartList.size() <= 0) {
                    showToast("No item available in cart.");
                    return;
                }
                adapter = new OrderConfirmAdapter(this, cartList);
                recyclerView.setAdapter(adapter);
                updateCartTotal(cartList);
            } else {
                showToast(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCartTotal(ArrayList<Cart> cartList) {
        float total = 0;
        for (int i = 0; i < cartList.size(); i++) {
            float quantity = Float.parseFloat(cartList.get(i).cartQuantity);
            float price = Float.parseFloat(cartList.get(i).price);
            total = total + (price * quantity);
        }
        float deliveryCharge = getDeliveryCharge();
        this.deliveryCharge = String.valueOf(deliveryCharge);
        float subTotal = total + deliveryCharge;
        tv_delivery_charge.setText("$" + numberFormat.format(deliveryCharge));
        tv_sub_total.setText("$" + numberFormat.format(subTotal));
    }

    private float getDeliveryCharge() {
        if (cartList == null || cartList.size() <= 0) return 0;
        String data = sharedpreferences.getString(Pref.deliveryCharge, "");
        float delCharge = 0;
        if (!TextUtils.isEmpty(data)) {
            List<DeliveryCharge> list = Utility.parseDeliveryCharge(data);
            delCharge = Float.parseFloat(list.get(list.size() - 1).price);
            Location source = new Location("Source");
            Location destination = new Location("Destination");
            source.setLatitude(Double.parseDouble(cartList.get(0).latitude));
            source.setLongitude(Double.parseDouble(cartList.get(0).longitude));
            destination.setLatitude(Double.parseDouble(address.latitude));
            destination.setLongitude(Double.parseDouble(address.longitude));

            float distance = source.distanceTo(destination);
            for (int i = 0; i < list.size(); i++) {
                if (distance <= Float.parseFloat(list.get(i).max_distance) && distance >= Float.parseFloat(list.get(i).min_distance)) {
                    return Float.parseFloat(list.get(i).price);
                }
            }
        }
        return delCharge;
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (TextUtils.isEmpty(result)) return;
        if (requestCode == GET_CART_ITEM_REQUEST_CODE) {
            parseResponse(result);
        } else if (requestCode == ORDER_REQUEST_CODE) {
            try {

                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {
                    Constant.cartCount = 0;
                    txtCounter.setText(String.valueOf(Constant.cartCount));

                    Intent intent = new Intent(OrderConfirmationActivity.this, MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                showToast(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
