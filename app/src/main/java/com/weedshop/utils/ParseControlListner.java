package com.weedshop.utils;

import com.android.volley.VolleyError;

public interface ParseControlListner {
    void onSuccess(String response,String method);

    void onError(VolleyError error,String method);
}
