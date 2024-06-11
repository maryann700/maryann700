package com.weedshop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Config;
import com.weedshop.utils.Constant;
import com.weedshop.utils.NotificationUtils;


/**
 * Created by mtpc on 4/2/16.
 */
public class BaseActivity extends FragmentActivity {

    protected SharedPreferences sharedpreferences;
    private AutoLogoutReceiver mLogoutReceiver;
    private IntentFilter mBroadCastFilter;
    private boolean isBroadCastReceiverRegister = false;


    private UpdateAppReceiver mUpdateAppReceiver;
    private IntentFilter mUpdateFileter;
    private boolean isUpdateReceiverRegister = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //QuickChippy application = (QuickChippy) getApplication();
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        mLogoutReceiver = new AutoLogoutReceiver();
        mBroadCastFilter = new IntentFilter();
        mBroadCastFilter.addAction(Config.LOGOUT_RECEIVER);

        mUpdateAppReceiver = new UpdateAppReceiver();
        mUpdateFileter = new IntentFilter();
        mUpdateFileter.addAction(Config.UPDATE_RECEIVER);

    }


    /*@Override
    protected void attachBaseContext(Context newBase) {

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //overridePendingTransition(R.anim.slide_in_back, R.anim.slide_out_back);
    }

    public void animatedStartActivityBack(Context context, Class intentClass) {
        // super.onBackPressed();
        final Intent intent = new Intent(context, intentClass);
        startActivity(intent);
        //overridePendingTransition(R.anim.slide_in_back, R.anim.slide_out_back);
    }

   /* @Override
    protected void onResume() {
        super.onResume();
        //Mint.initAndStartSession(BaseActivity.this, "fcfa9036");
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        //SomeTime Application Class load first than after loading activity
        //So we can manage maintain Auto logout By two logic
        /*1. Logic
         * if First Load the application class than after load activity
         * that time WeedApp.isLogout is true is Auto logout occur.
         * this logic run when logout receiver is not register but i get
         * app logout scenario work occur that time WeedApp.isLogout is true
         * and other logic will be start
         *  */
        /*2.Logic
         * if Activity load first than after Application class
         * load that time register.Logout receiver and than
         * open dialog from receiver
         * */

        //Execute when 1 Logic scenario is work
        if (WeedApp.isLogout) {
            WeedApp.isLogout = false;
            openForceLogoutDialog(WeedApp.logoutMessage);
        } else {
            //Execute when 2 Logic scenario is work
            isBroadCastReceiverRegister = true;
            registerReceiver(mLogoutReceiver, mBroadCastFilter);
        }
        //TODO UPDATE APP LOGIC
        //SomeTime Application Class load first than after loading activity
        //So we can manage maintain Auto logout By two logic
        /*1. Logic
         * if First Load the application class than after load activity
         * that time WeedApp.isUpdate is true is Update App scenario occur.
         * this logic run when UpdateApp receiver is not register but i get
         * app  Update App  scenario work occur that time WeedApp.isUpdate is true
         * and other logic will be start
         *  */
        /*2.Logic
         * if Activity load first than after Application class
         * load that time UpdateApp.Logout receiver and than
         * open Update App Activity
         * */

        //Execute when 1 Logic scenario is work
        if (WeedApp.isUpdate) {
            WeedApp.isUpdate = false;
            openUpdateActivity(true);
        } else {
            //Execute when 2 Logic scenario is work
            isUpdateReceiverRegister = true;
            registerReceiver(mUpdateAppReceiver, mUpdateFileter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isBroadCastReceiverRegister) {
            isBroadCastReceiverRegister = false;
            unregisterReceiver(mLogoutReceiver);
        }
        if (isUpdateReceiverRegister) {
            isUpdateReceiverRegister = false;
            unregisterReceiver(mUpdateAppReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * Logout Receiver
     */
    public class AutoLogoutReceiver extends BroadcastReceiver {
        public AutoLogoutReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // open force logout dialog
            openForceLogoutDialog(intent.getStringExtra("message"));
        }
    }

    /**
     * Update Receiver
     */
    public class UpdateAppReceiver extends BroadcastReceiver {
        public UpdateAppReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // open update activity
            openUpdateActivity(intent.getBooleanExtra("isUpdate", false));
        }
    }

    /**
     * Jump to update page if App Version  new in play store
     *
     * @param isUpdate
     */
    private void openUpdateActivity(boolean isUpdate) {
        if (isUpdate) {
            UpdateActivity.isShowingUpdateDialog = true;
            Intent intent = new Intent(this, UpdateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    /**
     * open force Logout Dialog
     *
     * @param message
     */
    private void openForceLogoutDialog(String message) {
        CommonUtils.showDialogNotCancelable(this, "Logout", message,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onLogout();
                    }
                });
    }

    /**
     * perform on logout event
     */
    private void onLogout() {
        // clear notification
        NotificationUtils.clearNotifications(getApplicationContext());

        //clear SharedPreferences
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.apply();

        //jump to sign in
        Intent m_intent = new Intent(BaseActivity.this, SignInActivity.class);
        m_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(m_intent);
    }
}
