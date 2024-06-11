package com.weedshop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.weedshop.Adapter.CartAdapter;
import com.weedshop.Adapter.ItemClickListener;
import com.weedshop.model.Cart;
import com.weedshop.model.NavDrawerItem;
import com.weedshop.model.Shop;
import com.weedshop.utils.CommonUtils;
import com.weedshop.utils.Config;
import com.weedshop.utils.Constant;
import com.weedshop.utils.JsonUtils;
import com.weedshop.utils.NotificationUtils;
import com.weedshop.utils.Pref;
import com.weedshop.utils.UnderlinedTextView;
import com.weedshop.webservices.AddUpdateDebit_credit_card_activty;
import com.weedshop.webservices.CommonTask;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by MTPC-83 on 6/24/2016.
 */
public class SideMenuActivity extends BaseActivity implements ItemClickListener {

    private static final String STATE_MENUDRAWER = "net.simonvt.menudrawer.samples.WindowSample.menuDrawer";
    public static String TAG = SideMenuActivity.class.getSimpleName();
    public MenuDrawer mMenuDrawer, mMenuDrawerRight;
    protected RelativeLayout rlCart;
    protected TextView txt_cancel;
    protected SharedPreferences sharedpreferences;
    NumberFormat numberFormat = new DecimalFormat("#,###.00");
    TextView txt_screen_title, txtCounter;
    TextView txt_screen_title2, txt_ride_share;
    TextView txt_vendor, txt_become_a_driver;
    UnderlinedTextView txt_shops_title;
    UnderlinedTextView txt_market_title;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private Toolbar toolbar;
    private ViewGroup mDrawerList;
    private CircleImageView profileImage;
    private ImageView ivCloseMenu;
    private TextView tvLogout, txtUserName;
    private RecyclerView rvCart;
    //private TextView txtEmptyCart;
    private FrameLayout txtEmptyCart;
    private CartAdapter cartAdapter;
    private ArrayList<Cart> cartList;
    private LinearLayout llCharge;
    private Button btnCheckout;
    private TextView tvItemsCount, tv_sub_total;
    private TextView tv_storeName;
    private AutoLogoutReceiver mLogoutReceiver;
    private IntentFilter mBroadCastFilter;
    private boolean isBroadCastReceiverRegister = false;


    private UpdateAppReceiver mUpdateAppReceiver;
    private IntentFilter mUpdateFileter;
    private boolean isUpdateReceiverRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        mLogoutReceiver = new AutoLogoutReceiver();
        mBroadCastFilter = new IntentFilter();
        mBroadCastFilter.addAction(Config.LOGOUT_RECEIVER);

        mUpdateAppReceiver = new UpdateAppReceiver();
        mUpdateFileter = new IntentFilter();
        mUpdateFileter.addAction(Config.UPDATE_RECEIVER);


    }


    public Toolbar initToolbar(boolean openManu) {
        mDrawerList = (ViewGroup) findViewById(R.id.list_slidermenu);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        rlCart = (RelativeLayout) findViewById(R.id.rlCart);
        ivCloseMenu = (ImageView) findViewById(R.id.iv_close_menu);
        tvLogout = (TextView) findViewById(R.id.tv_logout);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txt_cancel = (TextView) findViewById(R.id.txt_cancel);
        txt_screen_title = (TextView) findViewById(R.id.txt_screen_title);
        txt_screen_title2 = (TextView) findViewById(R.id.txt_screen_title2);
        txt_ride_share = (TextView) findViewById(R.id.txt_ride_share);
        txt_become_a_driver = (TextView) findViewById(R.id.txt_become_a_driver);
        txt_vendor = (TextView) findViewById(R.id.txt_vendor);


        txtCounter = (TextView) findViewById(R.id.txtCounter);

        txtCounter.setText(String.valueOf(Constant.cartCount));
        txt_cancel.setVisibility(View.GONE);
        rlCart.setVisibility(View.VISIBLE);
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutAlert(SideMenuActivity.this, "Are you sure want to logout?");
            }
        });
        setupList();
        ivCloseMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int drawerState = mMenuDrawer.getDrawerState();
                if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
                    mMenuDrawer.closeMenu();
                    return;
                } else {
                    mMenuDrawer.openMenu();
                }
                CommonUtils.hideKeyboard(SideMenuActivity.this, toolbar);
            }
        });
        profileImage = (CircleImageView) findViewById(R.id.profile_image);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SideMenuActivity.this, ProfileActivity.class);
                startActivity(intent);
                mMenuDrawer.closeMenu();
            }
        });

        if (openManu) mMenuDrawer.openMenu();

        mMenuDrawer.setDropShadow(new ColorDrawable(getResources().getColor(R.color.menu_shadow_color)));
        mMenuDrawer.setDropShadowSize(3);
        mMenuDrawerRight.setDropShadowSize(0);
        float width = getResources().getDisplayMetrics().widthPixels / 1.25f;
        mMenuDrawerRight.setMenuSize((int) width);
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
                final int drawerState = mMenuDrawer.getDrawerState();
                if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
                    mMenuDrawer.closeMenu();
                    return;
                } else {
                    mMenuDrawer.openMenu();
                }
                CommonUtils.hideKeyboard(SideMenuActivity.this, toolbar);
            }
        });
        rlCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedpreferences.getString("id", "") != null && !sharedpreferences.getString("id", "").isEmpty()) {
                    txtEmptyCart.setVisibility(View.GONE);
                    if (mMenuDrawerRight.isMenuVisible()) {
                        mMenuDrawerRight.closeMenu();
                    } else {
                        mMenuDrawerRight.openMenu();
                        CommonUtils.hideKeyboard(SideMenuActivity.this, ivCloseMenu);
                    }
                } else {
                    showLoginAlert();
                }
            }
        });

        mMenuDrawerRight.setOnDrawerStateChangeListener(new MenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == MenuDrawer.STATE_OPENING) {
                    txtEmptyCart.setVisibility(View.GONE);
                }
                if (newState == MenuDrawer.STATE_OPEN) {
                    VerifyUser();
                    getCartList();
                } else if (newState == MenuDrawer.STATE_CLOSED) {
                    updateCartAPI(false);
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {

            }
        });

        mMenuDrawer.setOnDrawerStateChangeListener(new MenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == MenuDrawer.STATE_OPEN) {
                    if (!TextUtils.isEmpty(sharedpreferences.getString(Pref.image, ""))) {
                        Glide.with(SideMenuActivity.this).load(sharedpreferences.getString(Pref.image, "")).placeholder(R.drawable.profile_img).listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                //  Log.e("Glide", "onException" + e.getMessage());
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                Log.e("Glide", "onResourceReady Success.");
                                profileImage.setImageResource(0);
                                profileImage.setBackgroundResource(0);
                                profileImage.setImageDrawable(resource);
                                return false;
                            }
                        }).into(profileImage);
                    }

                    txtUserName.setText(sharedpreferences.getString(Pref.name, ""));
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {

            }
        });

        navDrawerItems = new ArrayList<NavDrawerItem>();
        navDrawerItems.add(new NavDrawerItem("Shops", R.drawable.shop));
        navDrawerItems.add(new NavDrawerItem("Order history", R.drawable.order_history));
        navDrawerItems.add(new NavDrawerItem("Current order status", R.drawable.current_order));
        navDrawerItems.add(new NavDrawerItem("Terms of Use", R.drawable.term));
        navDrawerItems.add(new NavDrawerItem("Privacy Policy", R.drawable.privacy));
        navDrawerItems.add(new NavDrawerItem("Add Debit/Credit Card Details", R.drawable.card_icon));
        navDrawerItems.add(new NavDrawerItem("My Trip", R.drawable.card_icon));

        for (int i = 0; i < navDrawerItems.size(); i++) {
            final View layout2 = LayoutInflater.from(this).inflate(R.layout.drawer_list_item, mDrawerList, false);
            TextView txtTitle = (TextView) layout2.findViewById(R.id.titleqc);
            ImageView imgicon = (ImageView) layout2.findViewById(R.id.imgicon);
            txtTitle.setText(navDrawerItems.get(i).getTitle());
            imgicon.setImageResource(navDrawerItems.get(i).getId());
            layout2.setTag(i);
            mDrawerList.addView(layout2);
            final int position = i;
            layout2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = null;
                    switch (position) {
                        case 0:
                            intent = new Intent(SideMenuActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            mMenuDrawer.closeMenu();
                            break;
                        case 1:
                            if (sharedpreferences.getString("id", "") != null && !sharedpreferences.getString("id", "").isEmpty()) {
                                intent = new Intent(SideMenuActivity.this, OrderHistoryActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                mMenuDrawer.closeMenu();
                            } else {
                                showLoginAlert();
                            }
                            break;
                        case 2:
                            if (sharedpreferences.getString("id", "") != null && !sharedpreferences.getString("id", "").isEmpty()) {
                                intent = new Intent(SideMenuActivity.this, MapActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                mMenuDrawer.closeMenu();
                            } else
                                showLoginAlert();
                            break;

                        case 3:
                            String termsOfUse = "file:///android_asset/terms_condition.html";
                            intent = new Intent(SideMenuActivity.this, PrivacyPolicyActivity.class);
                            intent.putExtra("url", termsOfUse);
                            intent.putExtra("title", "Terms & Condition");
                            startActivity(intent);
                            mMenuDrawer.closeMenu();
                            break;
                        case 4:
                            String disclaimer = "http://high5delivery.com/privacy-policy";
                            intent = new Intent(SideMenuActivity.this, PrivacyPolicyActivity.class);
                            intent.putExtra("url", disclaimer);
                            intent.putExtra("title", "Privacy Policy");
                            startActivity(intent);
                            mMenuDrawer.closeMenu();
                            break;
                        case 5:
                            if (sharedpreferences.getString("id", "") != null && !sharedpreferences.getString("id", "").isEmpty()) {
                                intent = new Intent(SideMenuActivity.this, AddUpdateDebit_credit_card_activty.class);
                                intent.putExtra("title", "Add Debit/Credit Card");
                                startActivity(intent);
                                mMenuDrawer.closeMenu();
                            } else {
                                showLoginAlert();
                            }
                            break;
                            case 6:
                            if (sharedpreferences.getString("id", "") != null && !sharedpreferences.getString("id", "").isEmpty()) {
                                intent = new Intent(SideMenuActivity.this, MyTripActvity.class);
                                intent.putExtra("title", "My Trips");
                                startActivity(intent);
                                mMenuDrawer.closeMenu();
                           } else {
                               showLoginAlert();
                            }
                            break;
                    }
                }
            });
        }
        toolbar.setNavigationIcon(R.drawable.menu_icon);

        ImageButton imgMenu = getToolbarNavigationButton(toolbar);
        if (imgMenu != null) {
            imgMenu.setClickable(true);
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true);

            // it's probably a good idea to check if the color wasn't specified as a resource
            if (typedValue.resourceId != 0) {
                imgMenu.setBackgroundResource(typedValue.resourceId);
            } else {
                // this should work whether there was a resource id or not
                imgMenu.setBackgroundColor(typedValue.data);
            }
        }
        return toolbar;
    }

    private void showLoginAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                SideMenuActivity.this);
        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.str_login_required)
                .setCancelable(false)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        startActivity(new Intent(SideMenuActivity.this, SignInActivity.class));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private void logoutAlert(final Context context, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        // set dialog message
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if (CommonUtils.isNetworkConnected(context)) {
                            //logoutAPI();

                            //now static logout
                            onLogout();
                        } else {
                            showToast(context.getString(R.string.internet_error));
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }


    public void logoutAPI() {
        SharedPreferences prefs = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
        if (TextUtils.isEmpty(prefs.getString(Pref.id, "")) || prefs.getString(Pref.id, "").equalsIgnoreCase("null")) {
            Log.e(TAG, "User not LoggedIn");
            return;
        }

        SharedPreferences pref = getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("user_id", prefs.getString(Pref.id, ""));
        params.put("token", regId);

        new AsyncTask<Void, Void, String>() {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(SideMenuActivity.this);
                dialog.setMessage("Please wait");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                return CommonTask.getResponse(Constant.logout, params);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String msg = jsonObject.getString("msg");
                        boolean response = jsonObject.getBoolean("response");
                        if (response) {
                             /*  SharedPreferences prefsVerify = context.getSharedPreferences(Constant.VERIFICATION_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefsVerify.edit();
                        editor.clear();
                        editor.apply();*/

                            onLogout();
                            mMenuDrawer.closeMenu();
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

    protected void getCartList() {

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("user_id", sharedpreferences.getString(Pref.id, ""));
        params.put("action", "list");
        new AsyncTask<Void, Void, String>() {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(SideMenuActivity.this);
                dialog.setMessage("Please wait");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                return CommonTask.getResponse(Constant.cart_api, params);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                parseResponse(result);
            }
        }.execute();
    }

    private void VerifyUser() {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("user_id", sharedpreferences.getString(Pref.id, ""));
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                return CommonTask.getResponse(Constant.user_Info, params);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String msg = jsonObject.getString("msg");
                        boolean response = jsonObject.getBoolean("response");
                        if (response) {
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            JSONObject data = jsonObject.getJSONObject("data");
                            editor.putString(Pref.verificationCode, data.getString(Pref.verificationCode));
                            editor.putString(Pref.identificationId, data.getString(Pref.identificationId));
                            editor.putString(Pref.identificationPhoto, data.getString(Pref.identificationPhoto));
                            editor.putString(Pref.recommendationId, data.getString(Pref.recommendationId));
                            editor.putString(Pref.recommendationPhoto, data.getString(Pref.recommendationPhoto));
                            editor.putString(Pref.token, data.getString(Pref.token));
                            editor.putString(Pref.status, data.getString(Pref.status));
                            editor.putString(Pref.adminApproved, data.getString(Pref.adminApproved));
                            editor.putString(Pref.adminRejectReason, data.getString(Pref.adminRejectReason));
                            editor.putString(Pref.verifymsg, data.getString(Pref.verifymsg));
                            editor.apply();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public ImageButton getToolbarNavigationButton(Toolbar mToolbar) {
        int size = mToolbar.getChildCount();
        for (int i = 0; i < size; i++) {
            View child = mToolbar.getChildAt(i);
            if (child instanceof ImageButton) {
                ImageButton btn = (ImageButton) child;
                if (btn.getDrawable() == mToolbar.getNavigationIcon()) {
                    return btn;
                }
            }
        }
        return null;
    }

    private void setupList() {
        tv_storeName = (TextView) findViewById(R.id.tv_storeName);
        tvItemsCount = (TextView) findViewById(R.id.tvItemsCount);
        tv_sub_total = (TextView) findViewById(R.id.tv_sub_total);
        txtEmptyCart = (FrameLayout) findViewById(R.id.txtEmptyCart);
        rvCart = (RecyclerView) findViewById(R.id.rvCart);
        rvCart.setNestedScrollingEnabled(false);
        rvCart.setLayoutManager(new LinearLayoutManager(this));

        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.line_divider);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(dividerDrawable);
        rvCart.addItemDecoration(itemDecoration);
        llCharge = (LinearLayout) findViewById(R.id.ll_charge);
        btnCheckout = (Button) findViewById(R.id.btn_checkout);

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedpreferences = getSharedPreferences(Constant.LOGIN_USER_PREF, Context.MODE_PRIVATE);
                String approved = sharedpreferences.getString(Pref.adminApproved, "");
                if (TextUtils.isEmpty(approved)) return;
                if (approved.equalsIgnoreCase("Approved")) {
                    mMenuDrawerRight.closeMenu();
                    updateCartAPI(true);
                } else {
                    mMenuDrawerRight.closeMenu();
                    Intent intent = new Intent(SideMenuActivity.this, VerifyProcessActivity.class);
                    startActivity(intent);
                }
            }
        });
        tv_storeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartAdapter != null && cartAdapter.getList().size() > 0) {
                    mMenuDrawerRight.closeMenu();
                    Cart cart = cartAdapter.getList().get(0);
                    Shop shop = new Shop();
                    shop.id = cart.storeId;
                    shop.name = cart.storeName;
                    Intent intent = new Intent(SideMenuActivity.this, ShopProductsActivity.class);
                    intent.putExtra("data", shop);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mMenuDrawer.restoreState(inState.getParcelable(STATE_MENUDRAWER));
        mMenuDrawerRight.restoreState(inState.getParcelable(STATE_MENUDRAWER));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (txtCounter == null) return;
        txtCounter.setText(String.valueOf(Constant.cartCount));

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // if (Constant.fromCommentScreen == 0) {
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawer.saveState());
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawerRight.saveState());
        //  }
    }

    protected void setUpMenu(Activity activity, int layout) {
        mMenuDrawer = MenuDrawer.attach(activity, MenuDrawer.Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
        mMenuDrawer.setContentView(layout);
        mMenuDrawer.setMenuView(R.layout.activity_base_menu);
        mMenuDrawerRight = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY, Position.RIGHT, MenuDrawer.MENU_DRAG_WINDOW);
        mMenuDrawerRight.setMenuView(R.layout.activity_side_menu_right);
        initToolbar(false);
    }

    @Override
    public void onBackPressed() {
        if (mMenuDrawer.isMenuVisible()) {
            mMenuDrawer.closeMenu();
            return;
        }
        if (mMenuDrawerRight.isMenuVisible()) {
            mMenuDrawerRight.closeMenu();
            return;
        }
        super.onBackPressed();
    }

    private void parseResponse(String result) {
        if (TextUtils.isEmpty(result)) {
            llCharge.setVisibility(View.GONE);
            btnCheckout.setVisibility(View.GONE);
            txtEmptyCart.setVisibility(View.VISIBLE);
            return;
        }
        try {
            cartList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(result);
            String msg = jsonObject.getString("msg");
            boolean response = jsonObject.getBoolean("response");
            if (response) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Cart cart = new Cart();
                    cart.storeId = object.getString("store_id");
                    cart.storeName = object.getString("store_name");
                    cart.productId = object.getString("product_id");
                    cart.cartQuantity = object.getString("cart_quantity");
                    cart.totalQuantity = object.getString("total_quantity");
                    cart.name = object.getString("name");
                    cart.type = object.getString("type");
                    cart.color = object.getString("color");
                    cart.price = object.getString("price");
                    cart.image = object.getString("image");
                    cart.latitude = object.getString("latitude");
                    cart.longitude = object.getString("longitude");
                    cart.imageUrl = object.getString("image_url");

                    if (!TextUtils.isEmpty(object.getString("attributes"))) {
                        List<Cart.Attribute> attribList = new ArrayList<>();
                        JSONArray arrayAttrib = object.getJSONArray("attributes");
                        for (int j = 0; j < arrayAttrib.length(); j++) {
                            JSONObject attrib = arrayAttrib.getJSONObject(j);
                            Cart.Attribute attribute = new Cart.Attribute();
                            attribute.attributeId = attrib.getString("attribute_id");
                            attribute.attributeText = attrib.getString("attribute_text");
                            attribute.name = attrib.getString("name");
                            attribList.add(attribute);
                        }
                        cart.attributes = attribList;
                    }
                    cartList.add(cart);
                }
                if (cartList != null && cartList.size() > 0) {
                    tv_storeName.setText(cartList.get(0).storeName);
                    llCharge.setVisibility(View.VISIBLE);
                    btnCheckout.setVisibility(View.VISIBLE);
                    txtEmptyCart.setVisibility(View.GONE);
                } else {
                    tv_storeName.setText("");
                    llCharge.setVisibility(View.GONE);
                    btnCheckout.setVisibility(View.GONE);
                    txtEmptyCart.setVisibility(View.VISIBLE);
                }

                Constant.cartCount = cartList.size();
                txtCounter.setText(String.valueOf(Constant.cartCount));
                tvItemsCount.setText(String.valueOf(Constant.cartCount));

                cartAdapter = new CartAdapter(this, cartList);
                rvCart.setAdapter(cartAdapter);

                updateCartTotal(cartList);
            } else {
                showToast(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
          /*else if (ADD_CART_REQUEST_CODE == requestCode) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                boolean response = jsonObject.getBoolean("response");
                showToast(msg);
                if (response) {
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
    }

    private void updateCartAPI(boolean isRedirect) {
        if (cartAdapter == null) return;
        cartList = cartAdapter.getList();
        if (cartList != null && cartList.size() > 0) {
            String jsonArray = JsonUtils.getJsonArrayString(getProductList(cartList));
            final HashMap<String, String> params = new HashMap<String, String>();
            params.put("user_id", sharedpreferences.getString(Pref.id, ""));
            params.put("store_id", cartList.get(0).storeId);
            params.put("action", "update");
            params.put("product", jsonArray);
            if (isRedirect) {
                new AsyncTask<Void, Void, String>() {
                    ProgressDialog dialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = new ProgressDialog(SideMenuActivity.this);
                        dialog.setMessage("Please wait");
                        dialog.setCancelable(false);
                        dialog.show();
                    }

                    @Override
                    protected String doInBackground(Void... voids) {
                        return CommonTask.getResponse(Constant.cart_api, params);
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        super.onPostExecute(result);
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if (TextUtils.isEmpty(result)) return;
                        try {
                            cartList = new ArrayList<>();
                            JSONObject jsonObject = new JSONObject(result);
                            String msg = jsonObject.getString("msg");
                            boolean response = jsonObject.getBoolean("response");
                            if (response) {
                                cartAdapter = null;
                                if (mMenuDrawerRight.isMenuVisible()) mMenuDrawerRight.closeMenu();
                                Intent m_intent = new Intent(SideMenuActivity.this, CheckoutActivity.class);
                                m_intent.putExtra("total", tv_sub_total.getText().toString());
                                startActivity(m_intent);
                            } else {
                                showToast(msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.execute();
            } else {
                CommonTask task = new CommonTask(SideMenuActivity.this, Constant.cart_api, params, false);
                task.executeAsync();
            }
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        if (cartAdapter == null) return;
        cartList = cartAdapter.getList();
        updateCartTotal(cartList);
    }

    private void updateCartTotal(ArrayList<Cart> cartList) {
        if (cartList.size() <= 0) {
            btnCheckout.setVisibility(View.GONE);
            llCharge.setVisibility(View.GONE);
            txtEmptyCart.setVisibility(View.VISIBLE);
            tv_storeName.setText("");
        }
        Constant.cartCount = cartList.size();
        txtCounter.setText(String.valueOf(Constant.cartCount));
        tvItemsCount.setText(String.valueOf(Constant.cartCount));

        float total = 0;
        for (int i = 0; i < cartList.size(); i++) {
            float quantity = Float.parseFloat(cartList.get(i).cartQuantity);
            float price = Float.parseFloat(cartList.get(i).price);
            total = total + (price * quantity);
        }
        tv_sub_total.setText("$" + numberFormat.format(total));
    }

    private ArrayList<Map<String, Object>> getProductList(ArrayList<Cart> cartList) {
        ArrayList<Map<String, Object>> itemList = new ArrayList<>();
        for (int i = 0; i < cartList.size(); i++) {
            Map<String, String> hashMap = new HashMap<>();
            Map<String, Object> map = new HashMap<>();

            hashMap.put("quantity", cartList.get(i).cartQuantity);
            hashMap.put("product_id", cartList.get(i).productId);

            ObjectMapper m = new ObjectMapper();
            map = m.convertValue(hashMap, Map.class);
            itemList.add(map);
        }
        return itemList;
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
        Intent m_intent = new Intent(SideMenuActivity.this, SignInActivity.class);
        m_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(m_intent);
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
}