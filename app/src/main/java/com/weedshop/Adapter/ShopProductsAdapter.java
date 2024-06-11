package com.weedshop.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.weedshop.R;
import com.weedshop.model.Product;
import com.weedshop.model.Shop;
import com.weedshop.model.Type;

import java.util.ArrayList;

/**
 * Created by MTPC-86 on 3/10/2017.
 */

public class ShopProductsAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Product> productList;
    private ArrayList<Type> typeList;

    public ShopProductsAdapter(Context context, ArrayList<Product> productList, ArrayList<Type> typeList) {
        mContext = context;
        this.productList = productList;
        this.typeList = typeList;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.shop_products_item, null);
            holder = new ViewHolder();
            holder.tvProductTitle = (TextView) convertView.findViewById(R.id.tv_product_title);
            holder.tvWeight = (TextView) convertView.findViewById(R.id.tv_weight);
            holder.tvCategory = (TextView) convertView.findViewById(R.id.tv_category);
            holder.tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
            holder.imgProduct = (ImageView) convertView.findViewById(R.id.imgProduct);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product data = productList.get(position);
        holder.tvProductTitle.setText(data.name);
        if (data.attributes != null && data.attributes.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (Product.Attribute attribute : data.attributes) {
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
            holder.tvWeight.setText(builder.toString());
        }
        holder.tvCategory.setText(data.type.toUpperCase());
        holder.tvPrice.setText("$" + data.price);

        if (!TextUtils.isEmpty(data.imageUrl)) {
            Glide.with(mContext).load(data.imageUrl).placeholder(R.drawable.logo).into(holder.imgProduct);
        }

        GradientDrawable bgShape = (GradientDrawable) holder.tvCategory.getBackground();
        bgShape.setColor(Color.parseColor(getColorCode(data.type)));
        return convertView;
    }

    private class ViewHolder {
        TextView tvProductTitle, tvWeight, tvCategory, tvPrice;
        ImageView imgProduct;
    }

    public void updateList(ArrayList<Product> newList) {
        productList.addAll(newList);
        notifyDataSetChanged();
    }

    public ArrayList<Product> getList() {
        return productList;
    }

    private String getColorCode(String type) {
        for (int i = 0; i < typeList.size(); i++) {
            if (type.equalsIgnoreCase(typeList.get(i).name)) {
                return typeList.get(i).color;
            }
        }
        return "#EBB22D";
    }
}
