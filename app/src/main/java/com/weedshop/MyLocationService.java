package com.weedshop;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.weedshop.utils.Constant;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import java.util.HashMap;

/**
 * Created by MTPC-83 on 5/11/2017.
 */

public class MyLocationService extends Service implements OnTaskCompleted {

    private static final String TAG = "MyLocationService";
    private SharedPreferences sharedpreferences;
    private Context context;
    private Handler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        context = this;
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        Log.e(TAG, "onCreate");
        initializeLocationManager(context);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(runnableLocation);
        }
    }

    private void initializeLocationManager(final Context context) {
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnableLocation, 30000);
    }

    Runnable runnableLocation = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(runnableLocation, 30000);
            Log.e(TAG, "Driver Location Called.");
            // update_driver_location api call when driver location change
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedpreferences.getString(Pref.id, ""));
            CommonTask task = new CommonTask(MyLocationService.this, Constant.current_user_on_map_status_api, map, false);
            task.executeAsync();
        }
    };

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (result != null && !TextUtils.isEmpty(result)) {
            Intent intent = new Intent("LocationUpdate");
            intent.putExtra("response", result);
            LocalBroadcastManager.getInstance(MyLocationService.this).sendBroadcast(intent);
        }
    }
}
