package com.weedshop.Controller;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.GsonBuilder;
import com.weedshop.MainActivity;
import com.weedshop.R;
import com.weedshop.utils.ParseControlListner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateCustomerStripeAccountController {
    public void createCustomerAccountToStripe(final Context context, final String username, final String useremail, final ParseControlListner listner) {
        String url = "https://api.stripe.com/v1/customers";
        Log.e("test", "===create customer url===" + url);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

             Log.e("test","===create account response=="+ new GsonBuilder().setPrettyPrinting().create().toJson(response));
                if (!response.equals(null)) {
                    Log.e("Your Array Response", response);

                    listner.onSuccess(response,"add_cus_stripe");

                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
                listner.onError(error,"add_cus_stripe");
            }
        }) {

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",context.getResources().getString(R.string.key_secret_stripe));
                return headers;
            }

            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("description",username);
                params.put("email", useremail);

                Log.e("test", "===create customer parameter==" + params);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
