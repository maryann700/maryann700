package com.weedshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
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

public class SignUpActivity extends BaseActivity implements OnTaskCompleted {
    private static final int REQUEST_LOCATION = 2;
    private static String[] PERMISSIONS_LOCATION = {ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION};
    String stringLatitude, stringLongitude;
    GPSTracker gpsTracker;
    private TextView tvSignin, tvSignup;
    private EditText etName, etEmailAddress, etZipCode, etMobileNumber, etPassowrd;
    private Button btnSignUp;
    private SegmentedProgressBar segmentedProgressBar;
    private ImageView ivTerms, iv_privacy;
    private int click = 0, click1 = 0;
    private int clickShow = 0;
    private SharedPreferences sharedpreferences;
    private final String PREFIX = "+1";
    private TextView txtTermsOfUse, txtPrivacyPolicy, txtDisclaimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        tvSignin = findViewById(R.id.tv_signin);
        etName = findViewById(R.id.et_name);
        etEmailAddress = findViewById(R.id.et_emailaddress);
        etZipCode = findViewById(R.id.et_zipcode);
        etMobileNumber = findViewById(R.id.et_mobile);
        etPassowrd = findViewById(R.id.et_password);
        btnSignUp = findViewById(R.id.btn_signup);
        ivTerms = findViewById(R.id.iv_terms);
        iv_privacy = findViewById(R.id.iv_privacy);

        txtTermsOfUse = findViewById(R.id.txtTermsOfUse);
        txtPrivacyPolicy = findViewById(R.id.txtPrivacyPolicy);
        txtDisclaimer = findViewById(R.id.txtDisclaimer);

        txtDisclaimer.setText(CommonUtils.getSpannableString(txtDisclaimer.getText().toString()));

        txtTermsOfUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String termsOfUse = "file:///android_asset/terms_condition.html";
                Intent intent = new Intent(SignUpActivity.this, PrivacyPolicyActivity.class);
                intent.putExtra("url", termsOfUse);
                intent.putExtra("title", "Terms & Condition");
                startActivity(intent);
            }
        });

        txtPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String disclaimer = "http://high5delivery.com/privacy-policy";
                Intent intent = new Intent(SignUpActivity.this, PrivacyPolicyActivity.class);
                intent.putExtra("url", disclaimer);
                intent.putExtra("title", "Privacy Policy");
                startActivity(intent);
            }
        });

        txtDisclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String disclaimer = "file:///android_asset/disclaimer.html";
                Intent intent = new Intent(SignUpActivity.this, PrivacyPolicyActivity.class);
                intent.putExtra("url", disclaimer);
                intent.putExtra("title", "Legal Disclaimer");
                startActivity(intent);
            }
        });

        etMobileNumber.setText(PREFIX);
        Selection.setSelection(etMobileNumber.getText(), etMobileNumber.getText().length());

        etMobileNumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().contains(PREFIX)) {
                    etMobileNumber.setText(PREFIX);
                    Selection.setSelection(etMobileNumber.getText(), etMobileNumber.getText().length());
                }
            }
        });

        ivTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click == 0) {
                    ivTerms.setImageResource(R.drawable.img_terms);
                    click = 1;
                } else if (click == 1) {
                    //ivTerms.setBackgroundColor(Color.parseColor("#282B30"));
                    ivTerms.setImageResource(R.drawable.round_corner_terms);
                    click = 0;
                }
            }
        });

        iv_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click1 == 0) {
                    iv_privacy.setImageResource(R.drawable.img_terms);
                    click1 = 1;
                } else if (click1 == 1) {
                    //ivTerms.setBackgroundColor(Color.parseColor("#282B30"));
                    iv_privacy.setImageResource(R.drawable.round_corner_terms);
                    click1 = 0;
                }
            }
        });

        tvSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, 0);
                finish();
            }
        });

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
        segmentedProgressBar.setCompletedSegments(0);
        //FancyButton button1 = (FancyButton) findViewById(R.id.btn1);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etName.getText().toString().trim();
                String email = etEmailAddress.getText().toString().trim();
                String mobile = etMobileNumber.getText().toString().trim();
                if (mobile.length() > 3) {
                    mobile = mobile.substring(2);
                }
                String password = etPassowrd.getText().toString().trim();
                String zipcode = etZipCode.getText().toString().trim();
                if (checkData(username, email, mobile, password, zipcode, click, click1)) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("name", username);
                    map.put("email", email);
                    map.put("zipcode", zipcode);
                    map.put("mobile", mobile);
                    map.put("password", password);
                    CommonTask task = new CommonTask(SignUpActivity.this, Constant.register, map, true);
                    task.executeAsync();
                }
            }
        });

        etPassowrd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (etPassowrd.getRight() - etPassowrd.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 20)) {
                        // your action here
                        etPassowrd.setSelection(etPassowrd.getSelectionStart(), etPassowrd.getSelectionEnd());
                        if (clickShow == 0) {
                            etPassowrd.setTransformationMethod(new PasswordTransformationMethod());
                            clickShow = 1;
                        } else if (clickShow == 1) {
                            etPassowrd.setTransformationMethod(null);
                            clickShow = 0;
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        if (hasPermission(ACCESS_FINE_LOCATION) && hasPermission(ACCESS_COARSE_LOCATION)) {
            gpsTracker = new GPSTracker(SignUpActivity.this);
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

    boolean checkData(String username, String email, String mobile, String password, String zipcode, int click, int click1) {
        if (!CommonUtils.checkNetworkConnection(this, R.string.internet_error)) {
            return false;
        }

        if (username.length() == 0) {
            CommonUtils.toastShort(this, R.string.name_error);
            return false;
        }

        if (email.length() == 0) {
            CommonUtils.toastShort(this, R.string.email_error);
            return false;
        }

        if (email.length() != 0 && !CommonUtils.isValidEmail(email)) {
            CommonUtils.toastShort(this, R.string.email_valid_error);
            return false;
        }

        if (zipcode.length() == 0) {
            CommonUtils.toastShort(this, R.string.zipcode_error);
            return false;
        }

        if (zipcode.length() > 6) {
            CommonUtils.toastShort(this, R.string.zipcode_valid_error);
            return false;
        }

        if (mobile.length() == 0) {
            CommonUtils.toastShort(this, R.string.phone_error);
            return false;
        }

        if (mobile.length() != 0 && mobile.length() != 10 || !TextUtils.isDigitsOnly(mobile)) {
            CommonUtils.toastShort(this, R.string.mobile_valid_error);
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
        if (click1 == 0) {
            CommonUtils.toastShort(this, R.string.privacy_policy_error);
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

                    JSONObject jObject = jsonObject.getJSONObject("data");
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Pref.id, jObject.getString(Pref.id));
                    editor.apply();
                    WeedApp.updateUserDetail();
                    Intent m_intent = new Intent(SignUpActivity.this, VerificationActivity.class);
                    startActivity(m_intent);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, msg, Toast.LENGTH_LONG).show();
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(SignUpActivity.this, ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(SignUpActivity.this, ACCESS_FINE_LOCATION)) {
            requestPermissions(PERMISSIONS_LOCATION, REQUEST_LOCATION);
        } else {
            ActivityCompat.requestPermissions(SignUpActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
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
                    if (gpsTracker == null)
                        gpsTracker = new GPSTracker(SignUpActivity.this);
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
