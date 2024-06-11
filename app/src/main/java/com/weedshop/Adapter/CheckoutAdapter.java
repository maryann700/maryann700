package com.weedshop.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.weedshop.R;
import com.weedshop.model.Address;
import com.weedshop.utils.Constant;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by MTPC-86 on 3/10/2017.
 */

public class CheckoutAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Address> addressList;
    private ItemEditClickListener deleteListener;
    private SparseBooleanArray booleanArray;
    private SharedPreferences preferences;
    private CheckBox lastView = null;

    public CheckoutAdapter(Context c, ArrayList<Address> addressList) {
        mContext = c;
        deleteListener = (ItemEditClickListener) mContext;
        this.addressList = addressList;
        booleanArray = new SparseBooleanArray();
        preferences = mContext.getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
    }

    @Override
    public int getCount() {
        return addressList.size();
    }

    @Override
    public Object getItem(int position) {
        return addressList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.checkout_item, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            holder.txtPhone = (TextView) convertView.findViewById(R.id.txtPhone);
            holder.txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);
            holder.chkAddress = (CheckBox) convertView.findViewById(R.id.chkAddress);
            holder.imgEdit = (ImageView) convertView.findViewById(R.id.imgEdit);
            holder.imgDelete = (ImageView) convertView.findViewById(R.id.imgDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Address address = addressList.get(position);
        holder.txtName.setText(address.firstname + " " + address.lastname);
        holder.txtPhone.setText("+1" + address.phone);

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
        holder.txtAddress.setText(builder.toString());

        holder.chkAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (lastView != null) {
                        lastView.setChecked(false);
                    }
                    lastView = holder.chkAddress;
                    booleanArray.put(position, b);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Pref.lastAddress, address.id);
                    editor.apply();
                } else {
                    if (lastView == holder.chkAddress) {
                        lastView = null;
                    }
                    booleanArray.delete(position);
                }
            }
        });

        if (!TextUtils.isEmpty(preferences.getString(Pref.lastAddress, ""))) {
            if (preferences.getString(Pref.lastAddress, "").equalsIgnoreCase(address.id)) {
                holder.chkAddress.setChecked(true);
            } else {
                holder.chkAddress.setChecked(false);
            }
        }

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAddressDialog(mContext, "Are you sure want to delete?", addressList.get(position), position);
            }
        });

        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteListener.onItemEditClick(view, position);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.chkAddress.setChecked(!holder.chkAddress.isChecked());
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    private class ViewHolder {
        TextView txtName, txtPhone, txtAddress;
        ImageView imgEdit, imgDelete;
        CheckBox chkAddress;
    }

    public int getSelectedAddress() {
        if (booleanArray.size() > 0) {
            return booleanArray.keyAt(0);
        }
        return -1;
    }

    private void deleteAddressDialog(final Context context, String msg, final Address address, final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set dialog message
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("user_id", address.user_id);
                        params.put("action", "delete");
                        params.put("address_id", address.id);
                        CommonTask handler = new CommonTask(mContext, Constant.user_addresses, params, false);
                        handler.executeAsync();
                        addressList.remove(position);
                        notifyDataSetChanged();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }
}
