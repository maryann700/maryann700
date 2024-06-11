package com.weedshop.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.weedshop.R;
import com.weedshop.model.Cart;


import java.util.ArrayList;


public class MyTripAdapter extends RecyclerView.Adapter {
    private ArrayList<Cart> mDataSet = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;

    public MyTripAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_mytrip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, final int position) {


    }

    @Override
    public int getItemCount() {
        return 10;
    }



    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView  tv_tripid, tv_tripdistance, tv_tripfar, tv_date_time, tv_pickupaddress, tv_droffaddress, tv_driver_name, tv_driver_mobile;
        LinearLayout llDriver;
        Button btn_cancel;

        public ViewHolder(View itemView) {
            super(itemView);

            llDriver = itemView.findViewById(R.id.llDriver);
            tv_tripid = itemView.findViewById(R.id.tv_tripid);
            tv_tripdistance = itemView.findViewById(R.id.tv_tripdistance);
            tv_tripfar = itemView.findViewById(R.id.tv_tripfar);
            tv_date_time = itemView.findViewById(R.id.tv_date_time);
            tv_pickupaddress = itemView.findViewById(R.id.tv_pickupaddress);
            tv_droffaddress = itemView.findViewById(R.id.tv_droffaddress);
            tv_driver_name = itemView.findViewById(R.id.tv_driver_name);
            tv_driver_mobile =itemView.findViewById(R.id.tv_driver_mobile);
            btn_cancel = itemView.findViewById(R.id.btn_cancel);
        }
    }
}