package com.weedshop.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.weedshop.R;
import com.weedshop.SignInActivity;
import com.weedshop.custom.YesNoAlertDialogInterface;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by MTPC-83 on 6/15/2016.
 */
public class CommonUtils {
    private static AlertDialog dialog = null;

    //show short toast with string id
    public static void toastShort(Context c, int res) {
        Toast.makeText(c, res, Toast.LENGTH_SHORT).show();
    }

    //check if network available
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean checkNetworkConnection(final Context context, final int res) {
        if (isNetworkConnected(context)) {
            return true;
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    toastShort(context, res);
                }
            });
            return false;
        }
    }

    // check email validation
    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    // check mobile validation
    public final static boolean isValidMobile(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }

    public static boolean hideKeyboard(Activity activity) {
        // Check if no view has focus:
        boolean keyboardClosed = false;
        try {
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                keyboardClosed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyboardClosed;
    }

    public static void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static int getResourceId(Context context, String resName, String resType) {
        return context.getResources().getIdentifier(resName, resType, context.getPackageName());
    }

    public static void setImageGlideProductDetails(Context context, String image, ImageView imageView) {
        try {
            Glide.with(context).
                    load(image).
                    crossFade().
                    placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .fitCenter()
                    .dontAnimate()
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logoutAlert(final Context context, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set dialog message
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                  /*      SharedPreferences prefsVerify = context.getSharedPreferences(Constant.VERIFICATION_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefsVerify.edit();
                        editor.clear();
                        editor.apply();*/

                        SharedPreferences sharedpreferences = context.getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.clear();
                        editor.apply();
                        Intent m_intent = new Intent(context, SignInActivity.class);
                        m_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(m_intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public static SpannableString getSpannableString(String string) {
        SpannableString content = new SpannableString(string);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        return content;
    }

    public static boolean isCalifornia(Context context, double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            try {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
               /* Log.e("address", address);
                Log.e("city", city);
                Log.e("state", state);
                Log.e("country", country);
                Log.e("postalCode", postalCode);
                Log.e("knownName", knownName);*/

               /*for testing purpose, have added Gujarat as allowed state*/
//                return state.equalsIgnoreCase("California")
//                        || state.equalsIgnoreCase("Gujarat");
//                return state.equalsIgnoreCase("California");
                return true;
//
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("GeoCoder", "Failed to get Address.");
        }
        return true;
    }

    public static AlertDialog showDialogNotCancelable(Context context, String title, String message, DialogInterface.OnClickListener okListener) {
        try {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
            dialog = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", okListener)
                    .setCancelable(false)
                    .create();
            dialog.show();
            return dialog;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void openDialogWithYesNoButton(Context context, String message, String positiveButtonString,
                                                 String negativeButtonString,
                                                 final YesNoAlertDialogInterface callBack) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callBack.onPositiveButtonClicked();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(negativeButtonString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        callBack.onNegativeButtonClicked();
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }
}
