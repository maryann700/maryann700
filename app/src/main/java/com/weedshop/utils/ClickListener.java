package com.weedshop.utils;

import android.view.View;

/**
 * Created by MTPC-40 on 11/10/2016.
 */

public interface ClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
