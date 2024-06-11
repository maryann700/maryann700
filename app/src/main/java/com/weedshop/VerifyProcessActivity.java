package com.weedshop;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.weedshop.utils.Constant;
import com.weedshop.utils.Pref;

/**
 * Created by MTPC_51 on 4/17/2017.
 */
public class VerifyProcessActivity extends AppCompatActivity {

    private Button btn_contact_admin;
    private SharedPreferences sharedpreferences;
    private TextView txtVerifyMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_process);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);

        txtVerifyMsg = (TextView) findViewById(R.id.txtVerifyMsg);
        btn_contact_admin = (Button) findViewById(R.id.btn_contact_admin);

        txtVerifyMsg.setText(sharedpreferences.getString(Pref.verifymsg, ""));

        final String reason = sharedpreferences.getString(Pref.adminRejectReason, "");
        if (reason.equalsIgnoreCase("Identification,Recommendation")) {
            btn_contact_admin.setText("ADD IDENTIFICATION");
        } else if (reason.equalsIgnoreCase("Identification")) {
            btn_contact_admin.setText("ADD IDENTIFICATION");
        } else if (reason.equalsIgnoreCase("Recommendation")) {
            btn_contact_admin.setText("UPLOAD RECOMMENDATION");
        } else {
            btn_contact_admin.setText("CONTACT TO ADMIN");
        }

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

        btn_contact_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    if (reason.equalsIgnoreCase("Identification,Recommendation")) {
                    btn_contact_admin.setText("ADD IDENTIFICATION");
                    Intent intent = new Intent(VerifyProcessActivity.this, AddIdentificationActivity.class);
                    startActivity(intent);
                } else */

                    if (reason.equalsIgnoreCase("Identification")) {
                    btn_contact_admin.setText("ADD IDENTIFICATION");
                    Intent intent = new Intent(VerifyProcessActivity.this, AddIdentificationActivity.class);
                    intent.putExtra("redirect", true);
                    startActivity(intent);
                }
//                else if (reason.equalsIgnoreCase("Recommendation")) {
//                    btn_contact_admin.setText("UPLOAD RECOMMENDATION");
//                    Intent intent = new Intent(VerifyProcessActivity.this, UploadRecommendationActivity.class);
//                    // intent.putExtra("redirect", true);
//                    startActivity(intent);
//                }
                else {
                    btn_contact_admin.setText("CONTACT TO ADMIN");
                    String mailto = "mailto:testineed@gmail.com";
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse(mailto));
                    try {
                        startActivity(emailIntent);
                    } catch (ActivityNotFoundException e) {
                        //TODO: Handle case where no email app is available
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}