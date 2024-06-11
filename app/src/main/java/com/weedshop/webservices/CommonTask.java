package com.weedshop.webservices;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.weedshop.R;
import com.weedshop.utils.ConnectionDetector;
import com.weedshop.utils.Constant;
import com.weedshop.utils.OnTaskCompleted;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by MTPC-83 on 6/14/2016.
 */
public class CommonTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private OnTaskCompleted listener;
    private HashMap<String, String> params;
    private ProgressDialog dialog;
    private String response, url;
    private int requestCode = 0;
    private boolean showProgressDialog = true;

    public CommonTask(Context mContext, String url, HashMap<String, String> params, boolean showDialog) {
        this.mContext = mContext;
        this.params = params;
        this.url = url;
        this.listener = (OnTaskCompleted) mContext;
        showProgressDialog = showDialog;
    }

    public CommonTask(Activity activity, String url, HashMap<String, String> params, OnTaskCompleted listener, boolean showDialog) {
        this.mContext = activity;
        this.params = params;
        this.url = url;
        this.listener = listener;
        showProgressDialog = showDialog;
    }

    public CommonTask(Context mContext, String url, HashMap<String, String> params, int requestCode, boolean showDialog) {
        this.mContext = mContext;
        this.params = params;
        this.url = url;
        this.listener = (OnTaskCompleted) mContext;
        this.requestCode = requestCode;
        showProgressDialog = showDialog;
    }

    public CommonTask(Activity activity, String url, HashMap<String, String> params, OnTaskCompleted listener, int requestCode, boolean showDialog) {
        this.mContext = activity;
        this.params = params;
        this.url = url;
        this.listener = listener;
        this.requestCode = requestCode;
        showProgressDialog = showDialog;
    }

    public CommonTask(Fragment mContext, String url, HashMap<String, String> params, int requestCode, boolean showDialog) {
        this.mContext = mContext.getActivity();
        this.params = params;
        this.url = url;
        this.listener = (OnTaskCompleted) mContext;
        this.requestCode = requestCode;
        showProgressDialog = showDialog;
    }


    public CommonTask(Fragment mContext, String url, HashMap<String, String> params, boolean showDialog) {
        this.mContext = mContext.getActivity();
        this.params = params;
        this.url = url;
        this.listener = (OnTaskCompleted) mContext;
        showProgressDialog = showDialog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (showProgressDialog) {
            dialog = new ProgressDialog(mContext);
            dialog.setMessage("Please wait");
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    public static OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    public static String getResponse(String url, HashMap<String, String> params) {
        String response = "";
        try {
            final OkHttpClient client = getOkHttpClient();

            MultipartBody.Builder data = new MultipartBody.Builder();
            data.setType(MultipartBody.FORM);
            Log.e("Params", "" + params);
            Log.e("Url", "" + url);

            for (String key : params.keySet()) {
                    /*for image uploading*/
                if (key.equalsIgnoreCase("image") || key.equalsIgnoreCase("file")) {

                    File sourceFile = new File(params.get(key));

                    if (sourceFile.exists()) {
                        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                        data.addFormDataPart(key, "image.png", RequestBody.create(MEDIA_TYPE_PNG, sourceFile));
                    } else {
                        Log.e("CommonTask", "File doesn't exist in sdcard");
                    }
                } else {
                    data.addFormDataPart(key, params.get(key));
                }
            }

            RequestBody requestBody = data.build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("APPKEY", Constant.apikey)
                    .post(requestBody)
                    .build();

            Response res = client.newCall(request).execute();
            response = res.body().string();
            Log.e("responce ser_handle = ", response + "");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (ConnectionDetector.isConnection(mContext)) {
            try {
                final OkHttpClient client = getOkHttpClient();
                MultipartBody.Builder data = new MultipartBody.Builder();
                data.setType(MultipartBody.FORM);
                Log.e("Params", "" + params);
                Log.e("URL", "" + url);
                for (String key : params.keySet()) {
                    /*for image uploading*/
                    if (key.equalsIgnoreCase("image") || key.equalsIgnoreCase("file")) {
                        File sourceFile = new File(params.get(key));
                        if (sourceFile.exists()) {
                            MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                            data.addFormDataPart(key, "image.png", RequestBody.create(MEDIA_TYPE_PNG, sourceFile));
                        } else {
                            Log.e("CommonTask", "File doesn't exist in sdcard");
                        }
                    } else {
                        data.addFormDataPart(key, params.get(key));
                    }
                }

                RequestBody requestBody = data.build();
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("APPKEY", Constant.apikey)
                        .post(requestBody)
                        .build();

                Response res = client.newCall(request).execute();
                response = res.body().string();
                // Log.e("responce ser_handle == ", response + "");
                if (!res.isSuccessful()) throw new IOException("Unexpected code " + res);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            showToast(mContext.getString(R.string.internet_error));
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            if (showProgressDialog) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response != null && response.length() > 0) {
            Log.e("result ", url + "..." + response);
        }
        Log.e("Response", "" + new Gson().toJson(response));
        listener.onTaskCompleted(response, requestCode);
    }

    @Override
    protected void onCancelled() {
        try {
            if (showProgressDialog) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCancelled();
    }

    public void executeAsync() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            this.execute();
        }
    }

    private void showToast(final String toast) {
        try {
            Activity activity = (Activity) mContext;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
