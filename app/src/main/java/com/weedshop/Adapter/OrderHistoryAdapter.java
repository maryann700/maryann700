package com.weedshop.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.weedshop.R;
import com.weedshop.model.Order;

import java.util.ArrayList;

public class OrderHistoryAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private ArrayList<Order> orderList;
    private LayoutInflater mInflater;

    public OrderHistoryAdapter(Context context, ArrayList<Order> orderList) {
        mContext = context;
        this.orderList = orderList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, final int position) {
        final ViewHolder holder = (ViewHolder) h;
        Order data = orderList.get(position);

        if (!TextUtils.isEmpty(data.orderDate)) {
         /*   SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date newDate = null;
            try {
                newDate = format.parse(data.orderDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            format = new SimpleDateFormat("MMM dd yyyy");
            String date = format.format(newDate);
            holder.txtdate.setText("Date: " + date);*/
            holder.txtdate.setText("Date: " + data.orderDate);
            holder.txtdate.setVisibility(View.VISIBLE);
        } else {
            holder.txtdate.setVisibility(View.GONE);
        }

        Order.Product product = data.products.get(0);
        if (data.products.size() > 1) {
            holder.tv_more.setText(data.products.size() + " more");
            holder.tv_more.setVisibility(View.VISIBLE);
        } else {
            holder.tv_more.setVisibility(View.INVISIBLE);
        }
        holder.tvProductTitle.setText(product.productName);
        holder.tvWeight.setText(product.attributeDescription);
        holder.tvCategory.setText(product.type.toUpperCase());
        holder.tvPrice.setText("$" + data.finalTotal);
        if (!TextUtils.isEmpty(product.imageUrl)) {
            Glide.with(mContext).load(product.imageUrl).placeholder(R.drawable.logo).into(holder.imgProduct);
        }
        GradientDrawable bgShape = (GradientDrawable) holder.tvCategory.getBackground();
        bgShape.setColor(Color.parseColor(product.color));
    }

    @Override
    public int getItemCount() {
        if (orderList == null) return 0;
        return orderList.size();
    }

    public ArrayList<Order> getList() {
        return orderList;
    }

    public void updateList(ArrayList<Order> list) {
        orderList.addAll(list);
        notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductTitle, tvWeight, tvCategory, tvPrice, tv_more, txtdate;
        ImageView imgProduct;

        public ViewHolder(View itemView) {
            super(itemView);
            txtdate = (TextView) itemView.findViewById(R.id.txtdate);
            tvProductTitle = (TextView) itemView.findViewById(R.id.tv_product_title);
            tvWeight = (TextView) itemView.findViewById(R.id.tv_weight);
            tvCategory = (TextView) itemView.findViewById(R.id.tv_category);
            tvPrice = (TextView) itemView.findViewById(R.id.tv_price);
            tv_more = (TextView) itemView.findViewById(R.id.tv_more);
            imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
        }
    }
}