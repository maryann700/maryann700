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
import com.weedshop.R;
import com.weedshop.utils.ParseControlListner;

import java.util.HashMap;
import java.util.Map;

public class RetriveCardToStripeController {
    public void retriveCardToStripe(final Context context, String cus_id, final String token, final ParseControlListner lister) {

        String url = "https://api.stripe.com/v1/customers/" + cus_id + "/sources/" + token;
        Log.e("test", "===add card to stripe url===" + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                Log.e("Response", response);
                //Log.e("test","===create account response=="+ new GsonBuilder().setPrettyPrinting().create().toJson(response));
                if (!response.equals(null)) {
                    Log.e("Your Array Response", response);
                    lister.onSuccess(response,"retrive_card_stripe");

                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
                lister.onError(error,"retrive_card_stripe");
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
           /* @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("source", token);

                Log.e("test", "===retrive card token parameter==" + params);
                return params;
            }*/
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
