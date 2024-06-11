package com.weedshop.webservices;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.stripe.android.view.CardInputWidget;
import com.weedshop.Controller.AddCardToStripeController;
import com.weedshop.Controller.CreateCardTokenController;
import com.weedshop.Controller.RetriveCardToStripeController;
import com.weedshop.R;
import com.weedshop.utils.AESEncyption;
import com.weedshop.utils.Constant;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.ParseControlListner;
import com.weedshop.utils.Pref;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AddUpdateDebit_credit_card_activty extends AppCompatActivity implements ParseControlListner, OnTaskCompleted {
    private TextView txtTitle;
    //EditText et_name, edtcardno, etdate, etcvc;
    private CardInputWidget cardInputWidget;
    String cardno,exp,cvv,cus_stripe_id,cardtoken;
    Integer expmonth,expyear;
    private SharedPreferences sharedpreferences;
    Button btnsave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_debit_credit_card_activty);

        btnsave = findViewById(R.id.btn_save);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(getIntent().getStringExtra("title"));

        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        cardInputWidget = findViewById(R.id.cardInputWidget);

        cardtoken = sharedpreferences.getString(Pref.card_token, "");
        cardno = sharedpreferences.getString(Pref.cardno, "");
        exp = sharedpreferences.getString(Pref.exp, "");
        cvv = sharedpreferences.getString(Pref.cvv, "");
        cus_stripe_id =  sharedpreferences.getString(Pref.stripe_acct_id, "");


        Log.e("TAG", "--------------------");
        Log.e("TAG", "USER STRIPE ID ==> " + cus_stripe_id);
        Log.e("TAG", "CARD TOKEN ==> " + cardtoken);
        Log.e("TAG", "CARD NO ==> " + cardno);
        Log.e("TAG", "CARD EXP ==> " + exp);
        Log.e("TAG", "CARD CVV ==> " + cvv);
        Log.e("TAG", "--------------------");

        if(cardtoken != null && cardtoken.length() > 0)
        {
            //retrive card using api and set data
            RetriveCardToStripeController controller = new RetriveCardToStripeController();
            controller.retriveCardToStripe(getApplicationContext(),cus_stripe_id,cardtoken,this);
        }

        else {
            btnsave.setVisibility(View.VISIBLE);
        }

       /* if(cardno!=null && exp != null && cvv!= null && cardno.length() > 0 && exp.length() >0 && cvv.length() >0)
        {
            cardno = AESEncyption.decrypt(cardno);
            exp = AESEncyption.decrypt(exp);
            cvv = AESEncyption.decrypt(cvv);

            Log.e("TAG", "CARD NO ==> " + cardno);
            Log.e("TAG", "CARD EXP ==> " + exp);
            Log.e("TAG", "CARD CVV ==> " + cvv);

            String[] expf = exp.split("/");
            expmonth = Integer.valueOf(expf[0]);
            expyear = Integer.valueOf(expf[1]);

            cardInputWidget.setCardNumber(cardno.toString());
            cardInputWidget.setExpiryDate(expmonth,expyear);
            cardInputWidget.setCvcCode(cvv);
        }*/

        findViewById(R.id.txtBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

       // txtTitle = (TextView) findViewById(R.id.txtTitle);
       // txtTitle.setText(getIntent().getStringExtra("title"));

    }

    public void onBack(View view) {
    }

    public void Save(View view) {
      //  boolean error = dataFildReqireed();
       // if (error == false) {

          // cardInputWidget = new CardInputWidget(getApplicationContext());

           try {
               if(cardInputWidget.getCard()!=null) {
                   cardno = cardInputWidget.getCard().getNumber();
                   expmonth = cardInputWidget.getCard().getExpMonth();
                   expyear = cardInputWidget.getCard().getExpYear();
                   cvv = cardInputWidget.getCard().getCVC();

                   Log.e("TAG", "--------------------");
                   Log.e("TAG", "CARD NUMBER ==> " + cardno);
                   Log.e("TAG", "CARD EXP MONTH ==> " + expmonth);
                   Log.e("TAG", "CARD EXP YEAR ==> " + expyear);
                   Log.e("TAG", "CARD CVV ==> " + cvv);
                   Log.e("TAG", "--------------------");

                   //check card is valid or not then add card

                   CreateCardTokenController  controller = new CreateCardTokenController();
                   controller.cretecardToken(getApplicationContext(),cardno,expmonth,expyear,cvv,this);
               }
           }
           catch (NullPointerException e)
           {
               e.printStackTrace();
           }

           // Toast.makeText(this, "Card Details Saved", Toast.LENGTH_SHORT).show();
            //finish();
        //}
    }

    private void callApitoAddCardToStripe(String token) {

        AddCardToStripeController controller = new AddCardToStripeController();
        controller.addCardToStripe(getApplicationContext(),cus_stripe_id,token,this);
    }


    @Override
    public void onSuccess(String response,String method) {

        if(method.equalsIgnoreCase("card_token")) {
            JSONObject jdata = null;
            try {
                jdata = new JSONObject(response);

                String token = jdata.getString("id");
                Log.e("TAG", "==token id==" + token);

                if (token != null) {
                    callApitoAddCardToStripe(token);
                    //finish();
                    Toast.makeText(getApplicationContext(), "Card Details Saved", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(method.equalsIgnoreCase("add_card_stripe"))
        {
            /*try {
                cardno = AESEncyption.encrypt(cardno);
                exp = expmonth+"/"+expyear.toString().substring(2);
                exp = AESEncyption.encrypt(exp);
                cvv = AESEncyption.encrypt(cvv);

                Log.e("TAG", "AFTER ENCRYPTED");
                Log.e("TAG", "--------------------");
                Log.e("TAG", "CARD NUMBER ==> " + cardno);
                Log.e("TAG", "CARD EXP DATE ==> " + exp);
                Log.e("TAG", "CARD CVV ==> " + cvv);
                Log.e("TAG", "--------------------");

            } catch (Exception e) {
                e.printStackTrace();
            }*/
            JSONObject jdata = null;
            try {
                jdata = new JSONObject(response);

                String token = jdata.getString("id");
                Log.e("TAG", "==token id==" + token);

                if (token != null) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Pref.card_token, cardtoken);
                    editor.apply();
                    callApiToAddCardToDB(token);
                    Toast.makeText(getApplicationContext(), "Card Details Saved", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


           // callApiToAddCardToDB(cardno,exp,cvv);
        }

        if(method.equalsIgnoreCase("retrive_card_stripe"))
        {
            JSONObject jdata = null;
            try {
                jdata = new JSONObject(response);

                String cardno = jdata.getString("last4");
                int expmonth = jdata.getInt("exp_month");
                int expyear = jdata.getInt("exp_year");
                //String cvv = jdata.getString("id");

                Log.e("TAG", "--------------------");
                Log.e("TAG", "CARD NUMBER ==> " + cardno);
                Log.e("TAG", "CARD EXP MONTH ==> " + expmonth);
                Log.e("TAG", "CARD EXP YEAR ==> " + expyear);
                //Log.e("TAG", "CARD CVV ==> " + cvv);
                Log.e("TAG", "--------------------");

                cardInputWidget.setCardNumber(cardno);
                cardInputWidget.setExpiryDate(expmonth,expyear);
                cardInputWidget.setCvcCode("cvv");

                btnsave.setVisibility(View.GONE);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void callApiToAddCardToDB(String cardtoken) {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", sharedpreferences.getString(Pref.id, ""));
        map.put("cardToken", cardtoken);
        //map.put("cardno", cardno);
       //map.put("exp", exp);
       // map.put("cvv", cvv);
        map.put("action", "edit");
        CommonTask task = new CommonTask(AddUpdateDebit_credit_card_activty.this, Constant.add_update_card, map, true);
        task.executeAsync();
    }


    @Override
    public void onError(VolleyError error,String method) {

        if(method.equalsIgnoreCase("card_token") || method.equalsIgnoreCase("add_card_stripe")) {
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                switch (response.statusCode) {
                    case 402:
                        String json = new String(response.data);
                        Log.e("TAG", "Your error Response" + "Data Null" + json);
                        try {
                            JSONObject obj = new JSONObject(json);
                            Log.e("TAG", "error Response" + "Data Null" + obj);
                            JSONObject jdata = obj.getJSONObject("error");
                            String message = jdata.getString("message");
                            Log.e("TAG", "error msg" + "" + message);
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (!TextUtils.isEmpty(result)) {

            try {
                    JSONObject jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("msg");
                    boolean response = jsonObject.getBoolean("response");
                    if (response) {
                        finish();
                    } else {
                        Toast.makeText(AddUpdateDebit_credit_card_activty.this, msg, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /*private Boolean dataFildReqireed() {
        boolean error = true;
        if (etcvc.getText().toString().trim().equalsIgnoreCase("")) {
            etcvc.setError("Enter cvc");
        }
        else if (et_name.getText().toString().trim().equalsIgnoreCase("")) {
            et_name.setError("Enter Name");

        }
        else if (etdate.getText().toString().trim().equalsIgnoreCase("")) {
            etdate.setError("Enter Date");
        }
        else if (edtcardno.getText().toString().trim().equalsIgnoreCase("")) {
            edtcardno.setError("Enter Card Number");
        }else{
            error=false;
        }
        
        return error;
    }*/

}