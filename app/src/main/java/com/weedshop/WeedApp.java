package com.weedshop;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.weedshop.utils.Config;
import com.weedshop.utils.Constant;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class WeedApp extends MultiDexApplication {
    public static String TAG = WeedApp.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static Context mContext;
    //for app open close
    private ArrayList<String> runningActivities;
    private ActivityLifecycleCallbacks activityLifecycleCallbacks;

    // used for receiver is unregister and get auto logout
    public static boolean isLogout = false;
    public static String logoutMessage = "";
    // used for receiver is unregister and get app update
    public static boolean isUpdate = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        //Fabric.with(this, new Crashlytics());

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                updateUserDetail();
            }
        };

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        runningActivities = new ArrayList<>();
        activityLifecycleCallbacks = new LifecycleCallbacks();
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    public static synchronized Context getAppContext() {
        return mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        try {
            setAppTerminate();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
            unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onTerminate();
    }

    public int getRunningActivitySize() {
        return runningActivities == null ? 0 : runningActivities.size();
    }

    public void addRunningActivity(String name) {
        if (getRunningActivitySize() == 0) {
            Log.e(TAG, "App start from background .. *_*");
            //if user login is there, check if user logged in here
            updateUserDetail();
        }
        runningActivities.add(name);
        setAppVisible();
    }

    public void removeRunningActivity(final String name) {
        //delay remove for resume/pause issue
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runningActivities.remove(name);
                setAppVisible();
            }
        }, 500);
    }

    public void setAppVisible() {
        boolean visible = getRunningActivitySize() > 0;
        Context context = getAppContext();
        SharedPreferences prefLogin = context.getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        prefLogin.edit().putBoolean("AppState", visible).apply();
        if (!visible) {
            Log.e(TAG, "App went to background .. @_@");
        }
    }

    public void setAppTerminate() {
        Context context = getAppContext();
        SharedPreferences prefLogin = context.getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        prefLogin.edit().putBoolean("AppState", false).apply();
    }

    /*class for life cycle call backs*/
    class LifecycleCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.w(TAG, activity.getLocalClassName() + ".onResume");
            addRunningActivity(activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.w(TAG, activity.getLocalClassName() + ".onPause");
            removeRunningActivity(activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }


    public static void updateUserDetail() {
        final Context context = getAppContext();
        final SharedPreferences prefLogin = context.getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        final SharedPreferences pref = context.getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", "");
        String userId = prefLogin.getString(Pref.id, "");
        if (TextUtils.isEmpty(regId)) {
            Log.e(TAG, "onReceive reg id Empty");
            return;
        }
        if (TextUtils.isEmpty(userId) || userId.equalsIgnoreCase("null")) {
            Log.e(TAG, "User not LoggedIn");
            return;
        }

        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("user_id", userId);
        params.put("uniqueid", android_id);
        params.put("token", regId);
        params.put("device_type", Constant.ANDROID);
        //get Device Language
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);

        //get device os
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        params.put("device_os", String.valueOf(currentapiVersion));

        //get device model
        String model = Build.MODEL;
        params.put("device_model", model);

        //get device manufacture
        String manufacture = Build.MANUFACTURER;
        params.put("device_manufacturer", manufacture);

        //get app version(app_version = 1.0)
        int appVersion = BuildConfig.VERSION_CODE;
//        int appVersion = 11;
        params.put("app_version", String.valueOf(appVersion));

        //get app version code (buildversion = 1.0 .1)
        String buildversion = BuildConfig.VERSION_NAME;
//        String buildversion = "1.0.11";
        params.put("build_version", buildversion);

        //set build type
        params.put("build_type", BuildConfig.DEBUG ? "dev" : "prod");

        //set app type
        params.put("app_type", "customer");

        Log.e(TAG, "PARAM " + new Gson().toJson(params));

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
//                Log.d(TAG, "doInBackground: " + new Gson().toJson(params));
                return CommonTask.getResponse(Constant.user_device, params);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (!TextUtils.isEmpty(result)) {
                    Log.e("user_device<:>", result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String msg = jsonObject.getString("msg");
                        boolean response = jsonObject.getBoolean("response");
                        Log.e(TAG, "RESP " + jsonObject.toString());
                        if (response) {

                            prefLogin.edit().putString(Pref.deliveryCharge, jsonObject.getString("data")).apply();
                            //get user status object
                            JSONObject activeObj = jsonObject.getJSONObject("user_status");
                            //null check for activeObj
                            if (activeObj != null) {
                                //get status string
                                String status = activeObj.getString("status");
//                                Log.d(TAG, "onPostExecute: " + status);
                                //Check status string
                                //if status deactivated than user has been logout
                                if (status.equalsIgnoreCase("Deleted")) {
                                    //send broadcast for logout
                                    //which is receive at SideMenuActivity.Java
                                    Intent intent = new Intent();
                                    intent.putExtra("message", msg);
                                    intent.setAction(Config.LOGOUT_RECEIVER);
                                    getAppContext().sendBroadcast(intent);
                                    isLogout = true;
                                    logoutMessage = msg;
//                                    Log.d(TAG, "onPostExecute:asd " + jsonObject);
                                }

                                JSONObject jsonObjUpdate =
                                        jsonObject.getJSONObject("app version");
                                boolean isUpdateAvailable =
                                        jsonObjUpdate.getBoolean("status");
//                                Log.d(TAG, "isUpdateAvailable: " + isUpdateAvailable);
                                if (isUpdateAvailable) {
                                    //send broadcast for App Update
                                    //which is receive at SideMenuActivity.Java
                                    isUpdate = true;
                                    Intent intent = new Intent();
                                    intent.putExtra("isUpdate", true);
                                    intent.setAction(Config.UPDATE_RECEIVER);
                                    getAppContext().sendBroadcast(intent);
                                } else {
                                    //  get not update version available in play store
                                    // than send broadcast to SideMenuActivity.Java
                                    // CASE - 1: if app is not update than after update this
                                    // broadcast receive at UpdateActivity.java .Means once get
                                    // status true from api than after get status false.
                                    // CASE - 2: if app is not update than after update this
                                    // broadcast receive at UpdateActivity.java
                                    isUpdate = false;
                                    Intent intent = new Intent();
                                    intent.putExtra("isUpdate", false);
                                    intent.setAction(Config.UPDATE_RECEIVER);
                                    getAppContext().sendBroadcast(intent);
                                }
                            }
                            Log.e(TAG, msg);
                        } else {
                            Log.e(TAG, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "User Detail response not available");
                }
            }
        }.execute();
    }

    private static void startUpdateActivity(boolean checkVisible) {
        //if app visible check not required
       /* if (!checkVisible) {
            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    if (!UpdateActivity.isShowingUpdateDialog) {
                        UpdateActivity.isShowingUpdateDialog = true;

                    }
                }
            });
        } else {
            //check if app visible or not
            final boolean isVisible = mAppPrefs.getPrefAppVisible();
            if (isVisible) {
                showUpdatePending = false;
                //run on main thread
                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!UpdateActivity.isShowingUpdateDialog) {
                            UpdateActivity.isShowingUpdateDialog = true;
                            Intent intent = new Intent(getAppContext(), UpdateActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getAppContext().startActivity(intent);
                        }
                    }
                });
            } else {
                //make true to show update next time app launches
                showUpdatePending = true;
            }
        }*/
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: ");
                if (!UpdateActivity.isShowingUpdateDialog) {
                    UpdateActivity.isShowingUpdateDialog = true;
                    Intent intent = new Intent(getAppContext(), UpdateActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getAppContext().startActivity(intent);
                }
            }
        });
    }

}