package com.weedshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.util.Log;

import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Constant;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/*
 * This is a splash screen, will be display for a short time to user.
 */
public class SplashActivity extends BaseActivity /*implements OnTaskCompleted */ {
    private static final int REQUEST_LOCATION = 2;
    private static String[] PERMISSIONS_LOCATION = {ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION};
    private Handler m_handler;
    public static String TAG = SplashActivity.class.getSimpleName();
    String stringLatitude, stringLongitude;
    GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermission(ACCESS_FINE_LOCATION) || !hasPermission(ACCESS_COARSE_LOCATION)) {
            requestLocationPermissions();
            return;
        }

        gpsTracker = new GPSTracker(this);
        if (!gpsTracker.canGetLocation()) {
            gpsTracker.showSettingsAlert();
            return;
        }
        stringLatitude = String.valueOf(gpsTracker.getLatitude());
        stringLongitude = String.valueOf(gpsTracker.getLongitude());
        Constant.LATITUDE = stringLatitude;
        Constant.LONGITUDE = stringLongitude;
        Log.e("LAT LONG : ", stringLatitude + " - " + stringLongitude);

        boolean isCalifornia = CommonUtils.isCalifornia(this, gpsTracker.getLatitude(), gpsTracker.getLongitude());
        if (isCalifornia)
            splashScreen();
        else
            CommonUtils.showDialogNotCancelable(this, getString(R.string.app_name), "Currently we are not Serving in your City.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
    }

    private void splashScreen() {
        m_handler = new Handler();
        m_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
                // SharedPreferences prefsVerify = getSharedPreferences(Constant.VERIFICATION_PREF, Context.MODE_PRIVATE);
                Intent m_intent = null;
                /*if (TextUtils.isEmpty(prefs.getString(Pref.id, "")) || prefs.getString(Pref.id, "").equalsIgnoreCase("null")) {
                    m_intent = new Intent(SplashActivity.this, SignInActivity.class);
                    startActivity(m_intent);
                    overridePendingTransition(android.R.anim.fade_in, 0);
                    finish();
                } else if (TextUtils.isEmpty(prefs.getString(Pref.verifyCodeLocal, "")) || prefs.getString(Pref.verifyCodeLocal, "").equalsIgnoreCase("null")) {
                    m_intent = new Intent(SplashActivity.this, VerificationActivity.class);
                    startActivity(m_intent);
                    finish();
                } else if (TextUtils.isEmpty(prefs.getString(Pref.identificationPhoto, "")) || prefs.getString(Pref.identificationPhoto, "").equalsIgnoreCase("null")) {
                    m_intent = new Intent(SplashActivity.this, AddIdentificationActivity.class);
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

                else*/ {
                    m_intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(m_intent);
                    overridePendingTransition(android.R.anim.fade_in, 0);
                    finish();
                }
            }
        }, 3000);
    }

    /**
     * Requests the Contacts permissions.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    @SuppressLint("NewApi")
    private void requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            requestPermissions(PERMISSIONS_LOCATION, REQUEST_LOCATION);
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
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
                    if (gpsTracker == null) gpsTracker = new GPSTracker(this);
                    if (gpsTracker.canGetLocation()) {
                        boolean isCalifornia = CommonUtils.isCalifornia(this, gpsTracker.getLatitude(), gpsTracker.getLongitude());
                        if (!isCalifornia) {
                            CommonUtils.showDialogNotCancelable(this, getString(R.string.app_name), "Currently we are not Serving in your City.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                        } else {
                            splashScreen();
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

    @Override
    public void onBackPressed() {
        finish();
    }
}

