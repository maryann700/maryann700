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
import com.weedshop.model.Cart;

import java.util.ArrayList;

public class OrderConfirmAdapter extends RecyclerView.Adapter {
    private ArrayList<Cart> mDataSet = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;

    public OrderConfirmAdapter(Context context, ArrayList<Cart> dataSet) {
        mContext = context;
        mDataSet = dataSet;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.order_confirmation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, final int position) {
        final ViewHolder holder = (ViewHolder) h;
        final Cart data = mDataSet.get(position);

        holder.txtName.setText(data.name);
        holder.txtContains.setText("");
        if (data.attributes != null && data.attributes.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (Cart.Attribute attribute : data.attributes) {
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
            holder.txtContains.setText(builder.toString());
        }

        holder.tvFinalQuantity.setText(data.cartQuantity);

        if (!TextUtils.isEmpty(data.imageUrl)) {
            Glide.with(mContext).load(data.imageUrl).placeholder(R.drawable.logo).into(holder.imgProduct);
        }
        GradientDrawable bgShape = (GradientDrawable) holder.tv_category.getBackground();
        bgShape.setColor(Color.parseColor(data.color));
        holder.tvProductPrice.setText("$" + data.price);
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null) return 0;
        return mDataSet.size();
    }

    public ArrayList<Cart> getList() {
        return mDataSet;
    }

    public void updateList(ArrayList<Cart> list) {
        mDataSet = list;
        notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFinalQuantity, tvProductPrice, txtName, txtContains, tv_category;
        ImageView imgProduct;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFinalQuantity = (TextView) itemView.findViewById(R.id.tv_final_quantity);
            tvProductPrice = (TextView) itemView.findViewById(R.id.tv_product_price);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtContains = (TextView) itemView.findViewById(R.id.txtContains);
            tv_category = (TextView) itemView.findViewById(R.id.tv_category);
            imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
        }
    }
}