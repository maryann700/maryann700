package com.weedshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
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

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by MTPC-86 on 3/6/2017.
 */

public class SignInActivity extends BaseActivity implements OnTaskCompleted {
    private static final int REQUEST_LOCATION = 2;
    private static String[] PERMISSIONS_LOCATION = {ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION};
    TextView tvSignUp;
    Button btnSignin;
    private EditText edtEmail;
    private EditText editPassword;
    SharedPreferences sharedpreferences;
    String stringLatitude, stringLongitude;
    GPSTracker gpsTracker;
    private TextView txtTermsOfUse, txtDisclaimer;
    private int click = 0;
    ImageView iv_terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);

        tvSignUp = (TextView) findViewById(R.id.tv_signup);
        btnSignin = (Button) findViewById(R.id.btn_signin);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        editPassword = (EditText) findViewById(R.id.editPassword);
        iv_terms = (ImageView) findViewById(R.id.iv_terms);
        txtTermsOfUse = (TextView) findViewById(R.id.txtTermsOfUse);
        txtDisclaimer = (TextView) findViewById(R.id.txtDisclaimer);

        txtDisclaimer.setText(CommonUtils.getSpannableString(txtDisclaimer.getText().toString()));

        txtTermsOfUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String termsOfUse = "file:///android_asset/terms_condition.html";
                Intent intent = new Intent(SignInActivity.this, PrivacyPolicyActivity.class);
                intent.putExtra("url", termsOfUse);
                intent.putExtra("title", "Terms & Condition");
                startActivity(intent);
            }
        });

        txtDisclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String disclaimer = "file:///android_asset/disclaimer.html";
                Intent intent = new Intent(SignInActivity.this, PrivacyPolicyActivity.class);
                intent.putExtra("url", disclaimer);
                intent.putExtra("title", "Legal Disclaimer");
                startActivity(intent);
            }
        });

        iv_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click == 0) {
                    iv_terms.setImageResource(R.drawable.img_terms);
                    click = 1;
                } else if (click == 1) {
                    //ivTerms.setBackgroundColor(Color.parseColor("#282B30"));
                    iv_terms.setImageResource(R.drawable.round_corner_terms);
                    click = 0;
                }
            }
        });

        findViewById(R.id.txtForgot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, ForgotActivity.class);
                startActivity(intent);
            }
        });

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (hasPermission(ACCESS_FINE_LOCATION) && hasPermission(ACCESS_COARSE_LOCATION)) {

                    if (gpsTracker == null)
                        gpsTracker = new GPSTracker(SignInActivity.this);

                    if (gpsTracker.canGetLocation()) {
                        stringLatitude = String.valueOf(gpsTracker.getLatitude());
                        stringLongitude = String.valueOf(gpsTracker.getLongitude());
                        Constant.LATITUDE = stringLatitude;
                        Constant.LONGITUDE = stringLongitude;
                        Log.e("LAT LONG : ", stringLatitude + " - " + stringLongitude);

                        boolean isCalifornia = CommonUtils.isCalifornia(SignInActivity.this, gpsTracker.getLatitude(), gpsTracker.getLongitude());
                        if (!isCalifornia) {
                            CommonUtils.showDialogNotCancelable(SignInActivity.this, getString(R.string.app_name), "Currently we are not Serving in your City.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                        }
                    } else {
                        gpsTracker.showSettingsAlert();
                        return;
                    }
                } else {
                    requestLocationPermissions();
                    return;
                }

                String email = edtEmail.getText().toString();
                String password = editPassword.getText().toString();
                stringLatitude = String.valueOf(gpsTracker.getLatitude());
                stringLongitude = String.valueOf(gpsTracker.getLongitude());
                Constant.LATITUDE = stringLatitude;
                Constant.LONGITUDE = stringLongitude;
                if (checkData(email, password, click)) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("email", email);
                    map.put("password", password);
                    CommonTask task = new CommonTask(SignInActivity.this, Constant.login_api, map, true);
                    task.executeAsync();
                }
            }
        });
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent m_intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(m_intent);
                overridePendingTransition(android.R.anim.fade_in, 0);
                finish();
            }
        });

        if (hasPermission(ACCESS_FINE_LOCATION) && hasPermission(ACCESS_COARSE_LOCATION)) {
            gpsTracker = new GPSTracker(SignInActivity.this);
            if (gpsTracker.canGetLocation()) {
                stringLatitude = String.valueOf(gpsTracker.getLatitude());
                stringLongitude = String.valueOf(gpsTracker.getLongitude());
                Constant.LATITUDE = stringLatitude;
                Constant.LONGITUDE = stringLongitude;
                Log.e("LAT LONG : ", stringLatitude + " - " + stringLongitude);

                boolean isCalifornia = CommonUtils.isCalifornia(this, gpsTracker.getLatitude(), gpsTracker.getLongitude());
                if (!isCalifornia) {
                    CommonUtils.showDialogNotCancelable(this, getString(R.string.app_name), "Currently we are not Serving in your City.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                }
            } else {
                gpsTracker.showSettingsAlert();
                return;
            }
        } else {
            requestLocationPermissions();
            return;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //finish();
        finishAffinity();
    }

    private boolean checkData(String email, String password, int click) {
        if (!CommonUtils.checkNetworkConnection(this, R.string.internet_error)) {
            return false;
        }

        if (email.length() == 0) {
            CommonUtils.toastShort(this, R.string.hint_username);
            return false;
        }

        if (email.length() != 0 && !CommonUtils.isValidEmail(email)) {
            CommonUtils.toastShort(this, R.string.email_valid_error);
            return false;
        }

        if (password.length() == 0) {
            CommonUtils.toastShort(this, R.string.password_error);
            return false;
        }

        if (password.length() != 0 && password.length() < 8 || password.length() > 20) {
            CommonUtils.toastShort(this, R.string.password_length_error);
            return false;
        }

        if (click == 0) {
            CommonUtils.toastShort(this, R.string.term_condition_error);
            return false;
        }
        return true;
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {

                  /*  SharedPreferences prefsVerify = getSharedPreferences(Constant.VERIFICATION_PREF, Context.MODE_PRIVATE);
                    prefsVerify.edit().putString(Pref.verifyCodeLocal, "Verify").apply();*/

                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Pref.verifyCodeLocal, "Verify");
                    JSONObject data = jsonObject.getJSONObject("data");
                    editor.putString(Pref.id, data.getString(Pref.id));
                    editor.putString(Pref.name, data.getString(Pref.name));
                    editor.putString(Pref.email, data.getString(Pref.email));
                    editor.putString(Pref.password, data.getString(Pref.password));
                    editor.putString(Pref.address, data.getString(Pref.address));
                    editor.putString(Pref.zipcode, data.getString(Pref.zipcode));
                    editor.putString(Pref.mobile, data.getString(Pref.mobile));
                    editor.putString(Pref.verificationCode, data.getString(Pref.verificationCode));
                    editor.putString(Pref.identificationId, data.getString(Pref.identificationId));
                    editor.putString(Pref.identificationPhoto, data.getString(Pref.identificationPhoto));
                    editor.putString(Pref.recommendationId, data.getString(Pref.recommendationId));
                    editor.putString(Pref.recommendationPhoto, data.getString(Pref.recommendationPhoto));
                    editor.putString(Pref.token, data.getString(Pref.token));
                    editor.putString(Pref.status, data.getString(Pref.status));
                    editor.putString(Pref.adminApproved, data.getString(Pref.adminApproved));
                    editor.putString(Pref.adminRejectReason, data.getString(Pref.adminRejectReason));
                    editor.putString(Pref.birthdate, data.getString(Pref.birthdate));
                    editor.putString(Pref.image, data.getString(Pref.image));
                    editor.putString(Pref.verifymsg, data.getString(Pref.verifymsg));

                    editor.putString(Pref.stripe_acct_id, data.getString(Pref.stripe_acct_id));
                    editor.putString(Pref.admin_stripe_id, data.getString(Pref.admin_stripe_id));
                    editor.putString(Pref.card_token, data.getString(Pref.card_token));

                    editor.putString(Pref.cardno, data.getString(Pref.cardno));
                    editor.putString(Pref.exp, data.getString(Pref.exp));
                    editor.putString(Pref.cvv, data.getString(Pref.cvv));

                    if (data.getString(Pref.image) != null && !data.getString(Pref.image).startsWith("http")) {
                        editor.putString(Pref.image, Constant.IMAGE_PATH + data.getString(Pref.image));
                    }
                    editor.apply();
               /*     if (TextUtils.isEmpty(data.getString(Pref.verificationCode)) || data.getString(Pref.verificationCode).equalsIgnoreCase("null")) {
                        Intent m_intent = new Intent(SignInActivity.this, VerificationActivity.class);
                        startActivity(m_intent);
                        finish();
                        return;
                    }*/

                    if (!TextUtils.isEmpty(data.getString(Pref.adminApproved)) && data.getString(Pref.adminApproved).equalsIgnoreCase("Rejected")) {
                        Intent m_intent = new Intent(SignInActivity.this, VerifyProcessActivity.class);
                        startActivity(m_intent);
                        finish();
                        return;
                    }

                    if (TextUtils.isEmpty(data.getString(Pref.identificationPhoto)) || data.getString(Pref.identificationPhoto).equalsIgnoreCase("null")) {
                        Intent m_intent = new Intent(SignInActivity.this, AddIdentificationActivity.class);
                        startActivity(m_intent);
                        finish();
                        return;
                    }

                    //UN USED
//                    if (TextUtils.isEmpty(data.getString(Pref.recommendationPhoto)) || data.getString(Pref.recommendationPhoto).equalsIgnoreCase("null")) {
//                        Intent m_intent = new Intent(SignInActivity.this, UploadRecommendationActivity.class);
//                        startActivity(m_intent);
//                        finish();
//                        return;
//                    }

                    WeedApp.updateUserDetail();

                    Intent m_intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(m_intent);
                    finish();
                } else {
                    Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Requests the Contacts permissions.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    @SuppressLint("NewApi")
    private void requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this, ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this, ACCESS_FINE_LOCATION)) {
            requestPermissions(PERMISSIONS_LOCATION, REQUEST_LOCATION);
        } else {
            ActivityCompat.requestPermissions(SignInActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }
    }

    @SuppressLint("NewApi")
    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    /*Permissions*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //  showToast("Permission Granted, Now you can access camera.");
                    if (gpsTracker == null) gpsTracker = new GPSTracker(SignInActivity.this);
                    if (gpsTracker.canGetLocation()) {
                        boolean isCalifornia = CommonUtils.isCalifornia(this, gpsTracker.getLatitude(), gpsTracker.getLongitude());
                        if (!isCalifornia) {
                            CommonUtils.showDialogNotCancelable(this, getString(R.string.app_name), "Currently we are not Serving in your City.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                        }
                    } else {
                        gpsTracker.showSettingsAlert();
                        return;
                    }
                } else {
                    // showToast("Permission Denied, You cannot access application.");
                    //code for deny
                    CommonUtils.showDialogNotCancelable(this, "Permission", "Please grant permissions to access application.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestLocationPermissions();
                        }
                    });
                }
                break;
        }
    }
}
