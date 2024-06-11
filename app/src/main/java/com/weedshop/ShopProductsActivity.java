package com.weedshop;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.weedshop.Adapter.ShopProductsAdapter;
import com.weedshop.model.Category;
import com.weedshop.model.Product;
import com.weedshop.model.Shop;
import com.weedshop.model.Type;
import com.weedshop.utils.Constant;
import com.weedshop.utils.EndlessScrollListener;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.webservices.CommonTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShopProductsActivity extends SideMenuActivity implements OnTaskCompleted {
    private static final int PRODUCT_LIST_REQUEST_CODE = 2001;
    private static final int CATEGORY_REQUEST_CODE = 2002;
    private static final int TYPE_REQUEST_CODE = 2003;
    LinearLayout llSearchView, llfilter;
    TextView txtCategory;
    ImageView ivSearchbtn;
    ListView lv_shop_product;
    Button btn_search_product;
    Spinner spinnerCat;
    private ArrayList<Product> productList;
    private int totalPages = 0;
    private Boolean isSearch = false;
    private String categoryId;
    private ArrayList<String> typeIdList;

    private ShopProductsAdapter productsAdapter;
    private ArrayList<Category> catList;
    private ArrayList<Type> typeList;
    private Shop shop;
    private HorizontalScrollView hc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpMenu(this, R.layout.activity_shop_products);
        llSearchView = (LinearLayout) findViewById(R.id.llSearchView);
        txtCategory = (TextView) findViewById(R.id.txtCategory);
        llfilter = (LinearLayout) findViewById(R.id.llfilter);
        hc = (HorizontalScrollView) findViewById(R.id.hc);

        Bundle b = getIntent().getExtras();
        shop = (Shop) b.getSerializable("data");

        txt_screen_title.setText(shop.name);

        HashMap<String, String> map = new HashMap<>();
        map.put("type", "category");
        CommonTask task = new CommonTask(ShopProductsActivity.this, Constant.common, map, CATEGORY_REQUEST_CODE, false);
        task.executeAsync();

        map = new HashMap<>();
        map.put("type", "type");
        task = new CommonTask(ShopProductsActivity.this, Constant.common, map, TYPE_REQUEST_CODE, false);
        task.executeAsync();

        lv_shop_product = (ListView) findViewById(R.id.lv_shop_product);

        ivSearchbtn = (ImageView) findViewById(R.id.ivSearchbtn);
        spinnerCat = (Spinner) findViewById(R.id.spn_filter_product);
        btn_search_product = (Button) findViewById(R.id.btn_search_product);

        lv_shop_product.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                if (totalItemsCount >= 10)
                    loadNextDataFromApi(page, false);
                // or loadNextDataFromApi(totalItemsCount);
                return true;
            }
        });

        spinnerCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (catList != null) {
                    txtCategory.setText(catList.get(position).name);
                    categoryId = catList.get(position).id;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lv_shop_product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(ShopProductsActivity.this, ProductViewActivity.class);
                intent.putExtra("data", productsAdapter.getList().get(position));
                startActivity(intent);
            }
        });

        btn_search_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (llSearchView.getVisibility() == View.VISIBLE) {
                    slideToTop(llSearchView);
                    llSearchView.setVisibility(View.GONE);
                }
                loadNextDataFromApi(1, true);
            }
        });
        ivSearchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (llSearchView.getVisibility() == View.VISIBLE) {
                    slideToTop(llSearchView);
                    llSearchView.setVisibility(View.GONE);
                } else {
                    slideToBottom(llSearchView);
                    llSearchView.setVisibility(View.VISIBLE);
                }
            }
        });
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

    private void loadNextDataFromApi(int page, boolean isSearch) {
        this.isSearch = isSearch;
        HashMap<String, String> map = new HashMap<>();
        map.put("page", page + "");
        map.put("store_id", shop.id);

        if (!TextUtils.isEmpty(categoryId))
            map.put("category_id", categoryId);

        if (typeIdList != null && typeIdList.size() > 0)
            map.put("type_id", TextUtils.join(",", typeIdList));

        CommonTask task = new CommonTask(ShopProductsActivity.this, Constant.product_List, map, PRODUCT_LIST_REQUEST_CODE, true);
        task.executeAsync();
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (TextUtils.isEmpty(result))
            return;
        if (PRODUCT_LIST_REQUEST_CODE == requestCode) {
            productList = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");

                if (isSearch) {
                    productsAdapter = null;
                    lv_shop_product.setAdapter(productsAdapter);
                }

                if (response) {
                    totalPages = jsonObject.getInt("totalPages");
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Product product = new Product();
                        product.id = object.getString("id");
                        product.name = object.getString("name");
                        product.storeId = object.getString("store_id");
                        product.categoryId = object.getString("category_id");
                        product.typeId = object.getString("type_id");
                        product.image = object.getString("image");
                        product.price = object.getString("price");
                        product.quantity = object.getString("quantity");
                        product.description = object.getString("description");
                        product.status = object.getString("status");
                        product.createdDate = object.getString("created_date");
                        product.category = object.getString("category");
                        product.type = object.getString("type");
                        product.imageUrl = object.getString("image_url");

                        if (!TextUtils.isEmpty(object.getString("attributes"))) {
                            List<Product.Attribute> attribList = new ArrayList<>();
                            JSONArray arrayAttrib = object.getJSONArray("attributes");
                            for (int j = 0; j < arrayAttrib.length(); j++) {
                                JSONObject attrib = arrayAttrib.getJSONObject(j);
                                Product.Attribute attribute = new Product.Attribute();
                                attribute.attributeId = attrib.getString("attribute_id");
                                attribute.attributeText = attrib.getString("attribute_text");
                                attribute.name = attrib.getString("name");
                                attribList.add(attribute);
                            }
                            product.attributes = attribList;
                        }
                        if (object.getString("status").equalsIgnoreCase("Active"))
                            productList.add(product);
                    }

                    if (productsAdapter == null) {
                        productsAdapter = new ShopProductsAdapter(ShopProductsActivity.this, productList, typeList);
                        lv_shop_product.setAdapter(productsAdapter);
                    } else {
                        productsAdapter.updateList(productList);
                    }
                } else {
                    if (productsAdapter == null || productsAdapter.getList().size() <= 0) {
                        totalPages = 0;
                        productsAdapter = null;
                        lv_shop_product.setAdapter(productsAdapter);
                    }
                    Toast.makeText(ShopProductsActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (CATEGORY_REQUEST_CODE == requestCode) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {
                    catList = new ArrayList<>();
                    ArrayList<String> catNameList = new ArrayList<>();

                    Category category = new Category();
                    category.name = "All Category";
                    category.id = "";
                    category.status = "";
                    catNameList.add(category.name);
                    catList.add(category);

                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        category = new Category();
                        category.id = object.getString("id");
                        category.name = object.getString("name");
                        category.status = object.getString("status");

                        catList.add(category);
                        catNameList.add(object.getString("name"));
                    }
                    ArrayAdapter categoryAdapter = new ArrayAdapter(this, R.layout.spinner_text_item, catNameList);
                    spinnerCat.setAdapter(categoryAdapter);
                } else {
                    Toast.makeText(ShopProductsActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (TYPE_REQUEST_CODE == requestCode) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {
                    typeList = new ArrayList<>();
                    typeIdList = new ArrayList<>();

                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Type type = new Type();
                        type.id = object.getString("id");
                        type.name = object.getString("name");
                        type.color = object.getString("color");
                        type.status = object.getString("status");
                        typeList.add(type);
                    }
                    LayoutInflater inflater = LayoutInflater.from(this);
                    for (int k = 0; k < typeList.size(); k++) {
                        View filterView = inflater.inflate(R.layout.filter_view, null);
                        final LinearLayout layout = (LinearLayout) filterView.findViewById(R.id.llFilter);
                        for (int i = 0; i < layout.getChildCount(); i++) {

                            GradientDrawable bg = (GradientDrawable) layout.getBackground();
                            bg.setColor(Color.TRANSPARENT);

                            if (layout.getChildAt(i) instanceof TextView) {
                                ((TextView) layout.getChildAt(i)).setText(typeList.get(k).name);
                            }
                            if (layout.getChildAt(i) instanceof ImageView) {
                                GradientDrawable bgShape = (GradientDrawable) layout.getChildAt(i).getBackground();
                                bgShape.setColor(Color.parseColor(typeList.get(k).color));
                            }
                        }
                        final int pos = k;
                        layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (view.getTag().equals("0")) {
                                    layout.setTag("1");
                                    GradientDrawable bgShape = (GradientDrawable) layout.getBackground();
                                    bgShape.setColor(Color.parseColor(typeList.get(pos).color));
                                    bgShape = (GradientDrawable) layout.getChildAt(1).getBackground();
                                    bgShape.setColor(Color.WHITE);

                                    if (!typeIdList.contains(typeList.get(pos).id))
                                        typeIdList.add(typeList.get(pos).id);

                                } else {
                                    layout.setTag("0");
                                    GradientDrawable bgShape = (GradientDrawable) layout.getBackground();
                                    bgShape.setColor(Color.TRANSPARENT);
                                    bgShape = (GradientDrawable) layout.getChildAt(1).getBackground();
                                    bgShape.setColor(Color.parseColor(typeList.get(pos).color));

                                    if (typeIdList.contains(typeList.get(pos).id))
                                        typeIdList.remove(typeList.get(pos).id);
                                }
                                loadNextDataFromApi(1, true);
                            }
                        });
                        llfilter.addView(filterView);
                    }
                    loadNextDataFromApi(1, false);
                } else {
                    Toast.makeText(ShopProductsActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
