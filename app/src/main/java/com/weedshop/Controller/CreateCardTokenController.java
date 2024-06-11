package com.weedshop.Controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.weedshop.R;
import com.weedshop.utils.ParseControlListner;
import com.weedshop.webservices.AddUpdateDebit_credit_card_activty;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateCardTokenController {

    public void cretecardToken(final Context context, final String cardno, final Integer expmonth, final Integer expyear, final String cvv, final ParseControlListner lister) {
        String url = "https://api.stripe.com/v1/tokens";
        Log.e("test", "===create card token url===" + url);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                Log.e("Response", response);
                //Log.e("test","===create account response=="+ new GsonBuilder().setPrettyPrinting().create().toJson(response));
                if (!response.equals(null)) {
                    Log.e("Your Array Response", response);
                    lister.onSuccess(response,"card_token");

                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
                lister.onError(error,"card_token");
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
                params.put("card[number]", cardno);
                params.put("card[exp_month]", String.valueOf(expmonth));
                params.put("card[exp_year]", String.valueOf(expyear));
                params.put("card[cvc]", cvv);

                Log.e("test", "===create card token parameter==" + params);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
