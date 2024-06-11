package com.weedshop.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.weedshop.R;
import com.weedshop.model.Shop;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by MTPC-86 on 3/10/2017.
 */

public class ShopAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<Shop> shopList;

    public ShopAdapter(Context c, ArrayList<Shop> shopList) {
        mContext = c;
        this.shopList = shopList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return shopList.size();
    }

    @Override
    public Shop getItem(int position) {
        // TODO Auto-generated method stub
        return shopList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.activity_grid_item, null);
            viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
            viewHolder.imgShop = (ImageView) convertView.findViewById(R.id.imgShop);
            viewHolder.imgLogo = (ImageView) convertView.findViewById(R.id.imgLogo);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Shop shop = shopList.get(position);

        viewHolder.tv_title.setText(shop.name);
        viewHolder.tv_address.setText(shop.address);

        if (!TextUtils.isEmpty(shop.imageUrl)) {
            Glide.with(mContext).load(shop.imageUrl).bitmapTransform(new RoundedCornersTransformation(mContext, 10, 0,
                    RoundedCornersTransformation.CornerType.TOP)).into(viewHolder.imgShop);
        }

        if (!TextUtils.isEmpty(shop.logoUrl)) {
            Glide.with(mContext).load(shop.logoUrl).into(viewHolder.imgLogo);
        }

        return convertView;
    }

    public void updateList(ArrayList<Shop> newList) {
        shopList.addAll(newList);
        notifyDataSetChanged();
    }

    public class ViewHolder {
        TextView tv_title;
        TextView tv_address;
        ImageView imgShop, imgLogo;
    }
}
