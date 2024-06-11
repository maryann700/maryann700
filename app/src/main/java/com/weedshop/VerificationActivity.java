package com.weedshop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Constant;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by MTPC-86 on 3/6/2017.
 */

public class VerificationActivity extends BaseActivity implements OnTaskCompleted {

    private static final int RESEND_VERIFICATION_REQUEST_CODE = 2112;
    private static final int VERIFICATION_REQUEST_CODE = 3113;
    private Button btnSubmit;
    private SegmentedProgressBar segmentedProgressBar;
    private EditText et_name;
    private SharedPreferences sharedpreferences;
    private TextView txtResend;
    private ImageView imgLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        btnSubmit = (Button) findViewById(R.id.btn_submit);
        et_name = (EditText) findViewById(R.id.et_name);
        txtResend = (TextView) findViewById(R.id.txtResend);
        imgLogout = (ImageView) findViewById(R.id.imgLogout);

        segmentedProgressBar = (SegmentedProgressBar) findViewById(R.id.segmented_progressbar);
        // number of segments in your bar
        segmentedProgressBar.setSegmentCount(3);
        //empty segment color
        segmentedProgressBar.setContainerColor(Color.parseColor("#121317"));
        //fill segment color
        segmentedProgressBar.setFillColor(Color.parseColor("#2CCD9B"));
        //pause segment
        segmentedProgressBar.pause();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //play next segment specifying its duration
                segmentedProgressBar.playSegment(1000);

            }
        }, 600);
        //set filled segments directly
        segmentedProgressBar.setCompletedSegments(1);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(et_name.getText().toString().trim())) {

                    HashMap<String, String> map = new HashMap<>();
                    map.put("code", et_name.getText().toString().trim());
                    map.put("user_id", sharedpreferences.getString(Pref.id, ""));
                    CommonTask task = new CommonTask(VerificationActivity.this, Constant.register_verification, map, VERIFICATION_REQUEST_CODE, true);
                    task.executeAsync();
                } else {
                    Toast.makeText(VerificationActivity.this, "Please enter verification code.", Toast.LENGTH_LONG).show();
                }
            }
        });

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.logoutAlert(VerificationActivity.this, "Are you sure you want to Logout? Your Progress and account will be deleted. You can re-register with same email id.");
            }
        });


        txtResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtResend.getText().toString().equals("Resend Verification Code.")) {
                    //verification code send without 60sec
                    HashMap<String, String> map = new HashMap<>();
                    map.put("user_id", sharedpreferences.getString(Pref.id, ""));
                    CommonTask task = new CommonTask(VerificationActivity.this, Constant.resend_verification, map, RESEND_VERIFICATION_REQUEST_CODE, true);
                    task.executeAsync();
                }
            }
        });

       /* new CountDownTimer(60000, 1000) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                txtResend.setText("" + String.format("Resend Verification code after %d Second",
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)));
            }

            public void onFinish() {
                txtResend.setText("Resend Verification Code.");
            }
        }.start();*/
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (!TextUtils.isEmpty(result)) {
            if (requestCode == VERIFICATION_REQUEST_CODE) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("msg");
                    boolean response = jsonObject.getBoolean("response");
                    if (response) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(Pref.verifyCodeLocal, "Verify");
                        editor.apply();

                     /*   SharedPreferences prefsVerify = getSharedPreferences(Constant.VERIFICATION_PREF, Context.MODE_PRIVATE);
                        prefsVerify.edit().putString(Pref.verifyCodeLocal, "Verify").apply();*/

                        Toast.makeText(VerificationActivity.this, msg, Toast.LENGTH_LONG).show();
                        Intent m_intent = new Intent(VerificationActivity.this, AddIdentificationActivity.class);
                        startActivity(m_intent);
                        finish();
                    } else {
                        Toast.makeText(VerificationActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == RESEND_VERIFICATION_REQUEST_CODE) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("msg");
                    boolean response = jsonObject.getBoolean("response");
                    Toast.makeText(VerificationActivity.this, msg, Toast.LENGTH_LONG).show();
                    if (response) { }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
