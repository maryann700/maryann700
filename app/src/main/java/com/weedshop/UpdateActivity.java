package com.weedshop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.weedshop.utils.Config;
import com.weedshop.utils.Constant;
import com.weedshop.utils.Pref;

/**
 * Created by MTPC-133 on 1/24/2018.
 */

public class UpdateActivity extends AppCompatActivity {
    public static final String TAG = UpdateActivity.class.getSimpleName();
    public static boolean isShowingUpdateDialog = false;
    private UpdateReceiver mUpdateReceiver;
    private IntentFilter mUpdateFileter;
    private boolean isUpdateReceiverRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        mUpdateReceiver = new UpdateReceiver();
        mUpdateFileter = new IntentFilter();
        mUpdateFileter.addAction(Config.UPDATE_RECEIVER);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        TextView btnUpdate = (TextView) findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url;
                String applicationId = BuildConfig.APPLICATION_ID;
                try { // Check whether Google Play store is installed or not:
                    url = "market://details?id=" + applicationId;
                } catch (final Exception e) {
                    url = "https://play.google.com/store/apps/details?id=" + applicationId;
                }

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        isShowingUpdateDialog = true;
        if (!WeedApp.isUpdate) {
//            WeedApp.isUpdate = false;
            openUpdateActivity(false);
        } else {
            isUpdateReceiverRegister = true;
            registerReceiver(mUpdateReceiver, mUpdateFileter);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        isShowingUpdateDialog = false;
        if (isUpdateReceiverRegister) {
            Log.d(TAG, "onResume: 3");
            isUpdateReceiverRegister = false;
            unregisterReceiver(mUpdateReceiver);
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {

    }

    public class UpdateReceiver extends BroadcastReceiver {
        public UpdateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            openUpdateActivity(intent.getBooleanExtra("isUpdate", false));
        }
    }

    private void openUpdateActivity(boolean isUpdate) {
        if (!isUpdate) {
            SharedPreferences prefs = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
            // SharedPreferences prefsVerify = getSharedPreferences(Constant.VERIFICATION_PREF, Context.MODE_PRIVATE);
            Intent m_intent = null;
            if (TextUtils.isEmpty(prefs.getString(Pref.id, "")) || prefs.getString(Pref.id, "").equalsIgnoreCase("null")) {
                m_intent = new Intent(UpdateActivity.this, SignInActivity.class);
                startActivity(m_intent);
                overridePendingTransition(android.R.anim.fade_in, 0);
                finish();
            } else if (TextUtils.isEmpty(prefs.getString(Pref.verifyCodeLocal, "")) || prefs.getString(Pref.verifyCodeLocal, "").equalsIgnoreCase("null")) {
                m_intent = new Intent(UpdateActivity.this, VerificationActivity.class);
                startActivity(m_intent);
                finish();
            } else if (TextUtils.isEmpty(prefs.getString(Pref.identificationPhoto, "")) || prefs.getString(Pref.identificationPhoto, "").equalsIgnoreCase("null")) {
                m_intent = new Intent(UpdateActivity.this, AddIdentificationActivity.class);
                startActivity(m_intent);
                overridePendingTransition(android.R.anim.fade_in, 0);
                finish();
            }
            //                else if (TextUtils.isEmpty(prefs.getString(Pref.recommendationPhoto, "")) || prefs.getString(Pref.recommendationPhoto, "").equalsIgnoreCase("null")) {
            //                    m_intent = new Intent(SplashActivity.this, UploadRecommendationActivity.class);
            //                    startActivity(m_intent);
            //                    overridePendingTransition(android.R.anim.fade_in, 0);
            //                    finish();
            //                }

            else {
                Log.d(TAG, "openUpdateActivity: 12");
                m_intent = new Intent(UpdateActivity.this, MainActivity.class);
                startActivity(m_intent);
                overridePendingTransition(android.R.anim.fade_in, 0);
                finish();
            }
        }
    }

}