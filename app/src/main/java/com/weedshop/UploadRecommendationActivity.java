package com.weedshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Constant;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class UploadRecommendationActivity extends AppCompatActivity implements OnTaskCompleted {
    SegmentedProgressBar segmentedProgressBar;
    SharedPreferences sharedpreferences;
    private ImageView imgLogout;


    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int GALLERY_IMAGE_REQUEST_CODE = 200;
    private static final int REQUEST_CAMERA = 2;
    private static String[] PERMISSIONS_CAMERA = {CAMERA,
            WRITE_EXTERNAL_STORAGE};

    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri; // file url to store image/video
    private String imagepath = "";
    private static final String IMAGE_DIRECTORY_NAME = "WeedApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        setContentView(R.layout.activity_upload_recommendation);
        if (savedInstanceState != null)
            fileUri = savedInstanceState.getParcelable("file_uri");
        imgLogout = (ImageView) findViewById(R.id.imgLogout);
        segmentedProgressBar = (SegmentedProgressBar) findViewById(R.id.segmented_progressbar);
        // number of segments in your bar
        segmentedProgressBar.setSegmentCount(4);
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
        }, 100);
        //set filled segments directly
        segmentedProgressBar.setCompletedSegments(3);
        Button btnUploadCard = (Button) findViewById(R.id.btn_upload_card);

        btnUploadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (hasPermission(CAMERA) && hasPermission(WRITE_EXTERNAL_STORAGE)) {
                    selectImage();
                } else {
                    requestCameraPermissions();
                }
            }
        });
        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.logoutAlert(UploadRecommendationActivity.this, "Are you sure you want to Logout?");
            }
        });
    }

    /**
     * Requests the Contacts permissions.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    @SuppressLint("NewApi")
    private void requestCameraPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(UploadRecommendationActivity.this, CAMERA)
                || ActivityCompat.shouldShowRequestPermissionRationale(UploadRecommendationActivity.this, WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(PERMISSIONS_CAMERA, REQUEST_CAMERA);
        } else {
            ActivityCompat.requestPermissions(UploadRecommendationActivity.this, PERMISSIONS_CAMERA, REQUEST_CAMERA);
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

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        File file = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        imagepath = file.getAbsolutePath();
                        fileUri = FileProvider.getUriForFile(UploadRecommendationActivity.this, getPackageName() + ".provider", file);
                        // intent.setData(fileUri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else {
                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    // start the image capture Intent
                    startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            GALLERY_IMAGE_REQUEST_CODE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            showToast("Sorry! Your device doesn't support camera");
            // will close the app if the device does't have camera
        } else {
            builder.show();
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        // get the file url
        if (savedInstanceState != null)
            fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                try {
                    previewCapturedImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                showToast("User cancelled image capture");
            } else {
                // failed to capture image
                showToast("Sorry! Failed to capture image");
            }
        } else if (requestCode == GALLERY_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri selectedImageUri = data.getData();

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        File sourceFile = new File(getRealPathFromURI(data.getData()));
                        File destFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        copyFile(sourceFile, destFile);
                        imagepath = destFile.getAbsolutePath();
                    } else {
                        String[] projection = {MediaStore.MediaColumns.DATA};
                        CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                                null);
                        Cursor cursor = cursorLoader.loadInBackground();
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        cursor.moveToFirst();
                        String selectedImagePath = cursor.getString(column_index);
                        fileUri = Uri.parse(selectedImagePath);
                    }
                    previewCapturedImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                showToast("User cancelled image selection");
            } else {
                // failed to capture image
                showToast("Sorry! Failed to select image");
            }
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {

        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri,
                proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }


    private void previewCapturedImage() {
        try {
            // bitmap factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;
            Bitmap bitmap = null;

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                bitmap = BitmapFactory.decodeFile(imagepath,
                        options);
            } else {
                bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                        options);
            }
            if (bitmap != null) {
                if (!(Build.VERSION.SDK_INT > Build.VERSION_CODES.M)) {
                    imagepath = fileUri.getPath().toString();
                }

                uploadFile(imagepath);
            } else {
                Log.e("Camera Result:", "Bitmap null");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    private void showToast(String s) {
        Toast.makeText(UploadRecommendationActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * Checking device has camera hardware or not
     */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

    /*Permissions*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                    //  showToast("Permission Granted, Now you can access camera.");
                } else {
                    // showToast("Permission Denied, You cannot access camera.");
                    //code for deny
                    showDialogNotCancelable("Permission", "Please Grant Permissions to upload profile photo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestCameraPermissions();
                        }
                    });
                }
                break;
        }
    }

    private void showDialogNotCancelable(String title, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setCancelable(false)
                .create()
                .show();
    }


    private void uploadFile(String file) {
        if (!TextUtils.isEmpty(file)) {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", sharedpreferences.getString(Pref.id, ""));
            map.put("type", "medical_card");
            map.put("file", file);
            CommonTask task = new CommonTask(UploadRecommendationActivity.this, Constant.identificationPhoto_Upload, map, true);
            task.executeAsync();
        }
    }


    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                if (response) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Pref.recommendationPhoto, "done");
                    editor.apply();
                    Intent m_intent = new Intent(UploadRecommendationActivity.this, MainActivity.class);
                    startActivity(m_intent);
                    finish();
                } else {
                    Toast.makeText(UploadRecommendationActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
