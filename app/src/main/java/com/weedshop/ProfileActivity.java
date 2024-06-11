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
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;
import com.triggertrap.seekarc.SeekArc;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Constant;
import com.weedshop.utils.OnTaskCompleted;
import com.weedshop.utils.Pref;
import com.weedshop.webservices.CommonTask;

import net.simonvt.menudrawer.MenuDrawer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ProfileActivity extends SideMenuActivity implements DatePickerDialog.OnDateSetListener, OnTaskCompleted {
    // Camera and Gallery request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int GALLERY_IMAGE_REQUEST_CODE = 200;
    private String imagepath = "";
    private static final String IMAGE_DIRECTORY_NAME = "WeedApp";
    private static final int REQUEST_CAMERA = 2;
    private static String[] PERMISSIONS_CAMERA = {CAMERA,
            WRITE_EXTERNAL_STORAGE};
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri; // file url to store image/video
    private SeekArc seekround;
    int animate = 0;
    private RelativeLayout frame_profile;
    private ImageView imgProfile, imgFrame, imgEdit, imgTemp;
    private EditText edtName, edtDob, edtphone, edtemail, edtadd, edtstate, edtcardno;
    private View v1, v2, v3, v4, v5, v6;
    boolean isEditMode = false;
    private ViewGroup transitionsContainer;
    private final String PREFIX = "+1";
    private String serverDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpMenu(this, R.layout.activity_profile);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);

        if (savedInstanceState != null) fileUri = savedInstanceState.getParcelable("file_uri");
        txt_screen_title.setText(R.string.profile);
        mMenuDrawerRight.setTouchMode(MenuDrawer.TOUCH_MODE_NONE);
        rlCart.setVisibility(View.INVISIBLE);

        transitionsContainer = (ViewGroup) findViewById(R.id.transitions_container);
        frame_profile = (RelativeLayout) findViewById(R.id.frame_profile);
        seekround = (SeekArc) findViewById(R.id.seekround);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        imgFrame = (ImageView) findViewById(R.id.imgFrame);
        imgEdit = (ImageView) findViewById(R.id.imgEdit);
        imgTemp = (ImageView) findViewById(R.id.imgTemp);
        edtName = (EditText) findViewById(R.id.edtName);
        edtDob = (EditText) findViewById(R.id.edtDob);
        edtphone = (EditText) findViewById(R.id.edtphone);
        edtemail = (EditText) findViewById(R.id.edtemail);
        edtadd = (EditText) findViewById(R.id.edtadd);
        edtstate = (EditText) findViewById(R.id.edtstate);
        edtcardno = (EditText) findViewById(R.id.edtcardno);

        v1 = findViewById(R.id.v1);
        v2 = findViewById(R.id.v2);
        v3 = findViewById(R.id.v3);
        v4 = findViewById(R.id.v4);
        v5 = findViewById(R.id.v5);
        v6 = findViewById(R.id.v6);
     /*   v7 = findViewById(R.id.v7);*/

        edtphone.setText(PREFIX);
        Selection.setSelection(edtphone.getText(), edtphone.getText().length());

        updateUserInfo();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("user_id", sharedpreferences.getString(Pref.id, ""));
        map.put("action", "list");
        CommonTask task = new CommonTask(ProfileActivity.this, Constant.user_profile, map, true);
        task.executeAsync();

        txt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.hideKeyboard(ProfileActivity.this);
             /*   if (!isEditMode) {
                    seekround.setVisibility(View.INVISIBLE);
                }*/

                TransitionSet transitionSet = new TransitionSet();
                transitionSet.addTransition(new Fade());
                transitionSet.addTransition(new ChangeBounds());
                transitionSet.setDuration(1000);
                TransitionManager.beginDelayedTransition(transitionsContainer, transitionSet);

              /*  if (isEditMode) {
                    seekround.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Transition transition = new Fade();
                            transition.setDuration(500);
                            TransitionManager.beginDelayedTransition(llSeek, transition);
                            seekround.setVisibility(View.VISIBLE);
                        }
                    }, 1000);
                }*/
                if (isEditMode) {
                    isEditMode = false;
                    setDisableViews();
                    int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) frame_profile.getLayoutParams();
                    params.height = size;
                    params.width = size;
                    frame_profile.setLayoutParams(params);

                    size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) imgEdit.getLayoutParams();
                    params1.height = size;
                    params1.width = size;
                    imgEdit.setLayoutParams(params1);
                    imgEdit.setImageResource(R.drawable.edit_profile);
                }
                updateUserInfo();
            }
        });

        edtDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                Date date = null;
                SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
                try {
                    date = format.parse(sharedpreferences.getString(Pref.birthdate, ""));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date != null) now.setTime(date);
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        ProfileActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setMaxDate(Calendar.getInstance());
                dpd.show(getFragmentManager(), "Select Birth Date...");
            }
        });

        setDisableViews();
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEditMode && !isValidate()) {
                    return;
                }
               /* if (!isEditMode) {
                    seekround.setVisibility(View.INVISIBLE);
                }*/

                TransitionSet transitionSet = new TransitionSet();
                transitionSet.addTransition(new Fade());
                transitionSet.addTransition(new ChangeBounds());
                transitionSet.setDuration(1000);
                TransitionManager.beginDelayedTransition(transitionsContainer, transitionSet);

              /*  if (isEditMode) {
                    seekround.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Transition transition = new Fade();
                            transition.setDuration(500);
                            TransitionManager.beginDelayedTransition(llSeek, transition);
                            seekround.setVisibility(View.VISIBLE);
                        }
                    }, 1000);
                }*/

                if (isEditMode) {
                    String name = edtName.getText().toString().trim();
                    String phone = edtphone.getText().toString().trim();
                    String date = edtDob.getText().toString().trim();
                    String address = edtadd.getText().toString().trim();

                    if (phone.length() > 3) {
                        phone = phone.substring(2);
                    }
                    if (TextUtils.isEmpty(name)) {
                        showToast("Please enter name.");
                        return;
                    } else if (TextUtils.isEmpty(phone)) {
                        showToast("Please enter phone number.");
                        return;
                    } else if (TextUtils.isEmpty(date)) {
                        showToast("Please select birth date.");
                        return;
                    } else if (TextUtils.isEmpty(address)) {
                        showToast("Please enter address.");
                        return;
                    } else {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("user_id", sharedpreferences.getString(Pref.id, ""));
                        map.put("action", "edit");
                        map.put("name", name);
                        map.put("mobile", phone);
                        map.put("birthdate", serverDate);
                        map.put("address", address);
                        if (!TextUtils.isEmpty(imagepath))
                            map.put("image", imagepath);
                        CommonTask task = new CommonTask(ProfileActivity.this, Constant.user_profile, map, 3986, true);
                        task.executeAsync();
                    }

                    isEditMode = false;
                    setDisableViews();
                    int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) frame_profile.getLayoutParams();
                    params.height = size;
                    params.width = size;
                    frame_profile.setLayoutParams(params);

                    size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) imgEdit.getLayoutParams();
                    params1.height = size;
                    params1.width = size;
                    imgEdit.setLayoutParams(params1);
                    imgEdit.setImageResource(R.drawable.edit_profile);

                } else {
                    isEditMode = true;
                    setEnableViews();

                    int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) frame_profile.getLayoutParams();
                    params.height = size;
                    params.width = size;
                    frame_profile.setLayoutParams(params);

                    size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, getResources().getDisplayMetrics());
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) imgEdit.getLayoutParams();
                    params1.height = size;
                    params1.width = size;
                    imgEdit.setLayoutParams(params1);
                    imgEdit.setImageResource(R.drawable.save_profile);
                }
            }
        });

        imgEdit.setEnabled(false);
        final Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                seekround.setProgress(seekround.getProgress() + 1);
                if (seekround.getProgress() >= 100) {
                    animate = 1;
                    imgTemp.setVisibility(View.VISIBLE);
                    seekround.setVisibility(View.GONE);
                    imgEdit.setEnabled(true);
                } else {
                    handler.postDelayed(this, 30);
                }
            }
        };
        handler.postDelayed(r, 1500);

        frame_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isEditMode) return;
                CommonUtils.hideKeyboard(ProfileActivity.this);
                if (hasPermission(CAMERA) && hasPermission(WRITE_EXTERNAL_STORAGE)) {
                    selectImage();
                } else {
                    requestCameraPermissions();
                }
            }
        });

        edtphone.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().contains(PREFIX)) {
                    edtphone.setText(PREFIX);
                    Selection.setSelection(edtphone.getText(), edtphone.getText().length());
                }
            }
        });
    }

    private boolean isValidate() {
        String name = edtName.getText().toString().trim();
        String phone = edtphone.getText().toString().trim();
        String date = edtDob.getText().toString().trim();
        String address = edtadd.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            showToast("Please enter name.");
            return false;
        } else if (TextUtils.isEmpty(phone)) {
            showToast("Please enter phone number.");
            return false;
        } else if (TextUtils.isEmpty(date)) {
            showToast("Please select birth date.");
            return false;
        } else if (TextUtils.isEmpty(address)) {
            showToast("Please enter address.");
            return false;
        } else {
            return true;
        }
    }

    private void updateUserInfo() {
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        edtName.setText(sharedpreferences.getString(Pref.name, ""));
        serverDate = sharedpreferences.getString(Pref.birthdate, "");
        try {
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format1.parse(serverDate);
            SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
            String dd = format.format(date);
            edtDob.setText(dd);
        } catch (ParseException e) {
            e.printStackTrace();
            edtDob.setText("");
        }

        edtphone.setText(PREFIX + sharedpreferences.getString(Pref.mobile, ""));
        edtemail.setText(sharedpreferences.getString(Pref.email, ""));
        edtadd.setText(sharedpreferences.getString(Pref.address, ""));
        edtstate.setText(sharedpreferences.getString(Pref.identificationId, ""));
        edtcardno.setText(sharedpreferences.getString(Pref.recommendationId, ""));

        if (!TextUtils.isEmpty(sharedpreferences.getString(Pref.image, ""))) {
            Glide.with(this).load(sharedpreferences.getString(Pref.image, "")).placeholder(R.drawable.profile_img).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    // Log.e("Glide", "onException" + e.getMessage());
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    Log.e("Glide", "onResourceReady Success.");
                    imgProfile.setImageResource(0);
                    imgProfile.setBackgroundResource(0);
                    imgProfile.setImageDrawable(resource);
                    return false;
                }
            }).into(imgProfile);
        }
    }

    private void setDisableViews() {
        edtName.setEnabled(false);
        edtDob.setEnabled(false);
        edtphone.setEnabled(false);
        edtemail.setEnabled(false);
        edtadd.setEnabled(false);
        edtstate.setEnabled(false);
        edtcardno.setEnabled(false);
        imgFrame.setVisibility(View.INVISIBLE);
        // seekround.setVisibility(View.VISIBLE);
        v1.setVisibility(View.GONE);
        v2.setVisibility(View.GONE);
        v3.setVisibility(View.GONE);
        v4.setVisibility(View.GONE);
        v5.setVisibility(View.GONE);
        v6.setVisibility(View.GONE);
       /* v7.setVisibility(View.GONE);*/
        txt_cancel.setVisibility(View.INVISIBLE);
    }

    private void setEnableViews() {
        edtName.setEnabled(true);
        edtDob.setEnabled(true);
        edtphone.setEnabled(true);
        edtemail.setEnabled(true);
        edtadd.setEnabled(true);
        edtstate.setEnabled(true);
        edtcardno.setEnabled(true);
        //seekround.setVisibility(View.INVISIBLE);
        imgFrame.setVisibility(View.VISIBLE);
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.VISIBLE);
        v3.setVisibility(View.VISIBLE);
        v4.setVisibility(View.VISIBLE);
        v5.setVisibility(View.VISIBLE);
        v6.setVisibility(View.VISIBLE);
     /*   v7.setVisibility(View.VISIBLE);*/
        txt_cancel.setVisibility(View.VISIBLE);
    }

    /**
     * Requests the Contacts permissions.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    @SuppressLint("NewApi")
    private void requestCameraPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this, CAMERA)
                || ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this, WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(PERMISSIONS_CAMERA, REQUEST_CAMERA);
        } else {
            ActivityCompat.requestPermissions(ProfileActivity.this, PERMISSIONS_CAMERA, REQUEST_CAMERA);
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
                        fileUri = FileProvider.getUriForFile(ProfileActivity.this, getPackageName() + ".provider", file);
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

                // imgFrame.setImageBitmap(bitmap);
                imgProfile.setImageResource(0);
                imgProfile.setBackgroundResource(0);
                imgProfile.setImageBitmap(bitmap);
                // Glide.with(this).load(new File(imagepath)).placeholder(R.drawable.profile_img).into(imgProfile);
            } else {
                Log.e("Camera Result:", "Bitmap null");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
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
        Log.e("file path:", mediaFile.getAbsolutePath());
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

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
        String date = format.format(calendar.getTime());
        serverDate = "" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
        edtDob.setText(date);
    }

    @Override
    public void onTaskCompleted(String result, int requestCode) {
        if (TextUtils.isEmpty(result)) return;
        try {
            JSONObject jsonObject = new JSONObject(result);
            String msg = jsonObject.getString("msg");
            boolean response = jsonObject.getBoolean("response");
            if (response) {
                if (requestCode == 3986) showToast(msg);

                SharedPreferences.Editor editor = sharedpreferences.edit();
                JSONArray array = jsonObject.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject data = array.getJSONObject(i);
                    editor.putString(Pref.id, data.getString(Pref.id));
                    editor.putString(Pref.name, data.getString(Pref.name));
                    editor.putString(Pref.email, data.getString(Pref.email));
                    editor.putString(Pref.address, data.getString(Pref.address));
                    editor.putString(Pref.mobile, data.getString(Pref.mobile));
                    editor.putString(Pref.identificationId, data.getString(Pref.identificationId));
                    editor.putString(Pref.recommendationId, data.getString(Pref.recommendationId));
                    editor.putString(Pref.birthdate, data.getString(Pref.birthdate));
                    editor.putString(Pref.image, data.getString("image_url"));
                    editor.apply();
                    updateUserInfo();
                }
            } else {
                showToast(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
