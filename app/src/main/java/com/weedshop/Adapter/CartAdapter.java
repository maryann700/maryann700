package com.weedshop.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.weedshop.R;
import com.weedshop.model.Cart;
import com.weedshop.utils.Constant;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import java.util.ArrayList;
import java.util.HashMap;

public class CartAdapter extends RecyclerView.Adapter {
    private ArrayList<Cart> mDataSet = new ArrayList<>();
    private LayoutInflater mInflater;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    private Context mContext;
    ItemClickListener listener;
    int qtyCount = 1;
    int maxQty = 0;

    SharedPreferences sharedpreferences;

    public CartAdapter(Context context, ArrayList<Cart> dataSet) {
        mContext = context;
        mDataSet = dataSet;
        mInflater = LayoutInflater.from(context);
        listener = (ItemClickListener) context;
        sharedpreferences = mContext.getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        // uncomment if you want to open only one row at a time
        // binderHelper.setOpenOnlyOne(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.activity_swipe_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, final int position) {
        final ViewHolder holder = (ViewHolder) h;

        final Cart data = mDataSet.get(position);
        // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
        // put an unique string id as value, can be any string which uniquely define the data
        binderHelper.bind(holder.swipeLayout, data.productId);
        binderHelper.setOpenOnlyOne(true);

        holder.iv_delete_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("user_id", sharedpreferences.getString(Pref.id, ""));
                params.put("product_id", mDataSet.get(position).productId);
                params.put("action", "delete");
                CommonTask task = new CommonTask(mContext, Constant.cart_api, params, false);
                task.executeAsync();
                mDataSet.remove(position);
                //   notifyItemRemoved(position);
                notifyDataSetChanged();
                listener.onItemClick(v, position);
            }
        });

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
        holder.tvTotalQuantity.setText(data.cartQuantity);

        if (!TextUtils.isEmpty(data.cartQuantity))
            qtyCount = Integer.parseInt(data.cartQuantity);

        if (!TextUtils.isEmpty(data.totalQuantity))
            maxQty = Integer.parseInt(data.totalQuantity);

        if (maxQty <= 0) {
            holder.txtLeft.setVisibility(View.GONE);
            holder.tvFinalQuantity.setTextColor(Color.RED);
            holder.tvFinalQuantity.setText("SoldOut");
            holder.rlPlueQuantity.setEnabled(false);
            holder.rlMinusQuantity.setEnabled(false);
            qtyCount = maxQty;
            holder.tvTotalQuantity.setText(String.valueOf(qtyCount));
            holder.tvFinalQuantity.setText(String.valueOf(qtyCount));
            qtyCount = Integer.parseInt(holder.tvTotalQuantity.getText().toString());
            data.cartQuantity = String.valueOf(qtyCount);
            mDataSet.set(position, data);
            listener.onItemClick(null, position);
        } else if (maxQty < qtyCount) {
            qtyCount = maxQty;
            holder.rlPlueQuantity.setEnabled(false);
            holder.txtLeft.setVisibility(View.VISIBLE);
            holder.txtLeft.setText("Only " + maxQty + " quantity left");
            holder.tvTotalQuantity.setText(String.valueOf(qtyCount));
            holder.tvFinalQuantity.setText(String.valueOf(qtyCount));
            qtyCount = Integer.parseInt(holder.tvTotalQuantity.getText().toString());
            data.cartQuantity = String.valueOf(qtyCount);
            mDataSet.set(position, data);
            listener.onItemClick(null, position);
        } else {
            holder.txtLeft.setVisibility(View.GONE);
            holder.rlPlueQuantity.setEnabled(true);
            holder.rlMinusQuantity.setEnabled(true);
        }

        if (!TextUtils.isEmpty(data.imageUrl)) {
            Glide.with(mContext).load(data.imageUrl).placeholder(R.drawable.logo).into(holder.imgProduct);
        }

        GradientDrawable bgShape = (GradientDrawable) holder.tv_category.getBackground();
        bgShape.setColor(Color.parseColor(data.color));

        holder.tvProductPrice.setText("$" + data.price);

        holder.rlMinusQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qtyCount = Integer.parseInt(data.cartQuantity);
                if (qtyCount > 1) {
                    qtyCount--;
                    holder.tvTotalQuantity.setText(String.valueOf(qtyCount));
                    holder.tvFinalQuantity.setText(String.valueOf(qtyCount));
                } else {
                    holder.tvTotalQuantity.setText("1");
                }
                qtyCount = Integer.parseInt(holder.tvTotalQuantity.getText().toString());
                data.cartQuantity = String.valueOf(qtyCount);
                mDataSet.set(position, data);
                listener.onItemClick(view, position);
            }
        });
        holder.rlPlueQuantity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                qtyCount = Integer.parseInt(data.cartQuantity);
                if (qtyCount < maxQty) {
                    qtyCount++;
                    holder.tvTotalQuantity.setText(String.valueOf(qtyCount));
                    holder.tvFinalQuantity.setText(String.valueOf(qtyCount));
                    qtyCount = Integer.parseInt(holder.tvTotalQuantity.getText().toString());
                    data.cartQuantity = String.valueOf(qtyCount);
                    mDataSet.set(position, data);
                    listener.onItemClick(view, position);
                }
            }
        });

        holder.swipeLayout.setSwipeListener(new SwipeRevealLayout.SwipeListener() {
            @Override
            public void onClosed(SwipeRevealLayout view) {
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.background_color));
            }

            @Override
            public void onOpened(SwipeRevealLayout view) {
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.background_color_light));
            }

            @Override
            public void onSlide(SwipeRevealLayout view, float slideOffset) {

            }
        });
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

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     */
    public void saveStates(Bundle outState) {
        binderHelper.saveStates(outState);
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
     */
    public void restoreStates(Bundle inState) {
        binderHelper.restoreStates(inState);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private SwipeRevealLayout swipeLayout;
        ImageView rlPlueQuantity, rlMinusQuantity;
        TextView tvFinalQuantity, tvProductPrice, tvTotalQuantity, txtName, txtContains, tv_category, txtLeft;
        ImageView imgProduct, iv_delete_product;

        public ViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            rlMinusQuantity = (ImageView) itemView.findViewById(R.id.rl_minus_quantity);
            rlPlueQuantity = (ImageView) itemView.findViewById(R.id.rl_plus_quantity);
            tvTotalQuantity = (TextView) itemView.findViewById(R.id.tv_total_quantity);
            tvFinalQuantity = (TextView) itemView.findViewById(R.id.tv_final_quantity);
            tvProductPrice = (TextView) itemView.findViewById(R.id.tv_product_price);
            txtLeft = (TextView) itemView.findViewById(R.id.txtLeft);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtContains = (TextView) itemView.findViewById(R.id.txtContains);
            tv_category = (TextView) itemView.findViewById(R.id.tv_category);

            imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
            iv_delete_product = (ImageView) itemView.findViewById(R.id.iv_delete_product);
        }
    }
}