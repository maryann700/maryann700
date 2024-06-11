package com.weedshop.utils;

/**
 * Created by Ravi Tamada on 28/09/16.
 * www.androidhive.info
 */

public class Config {
    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 98;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 99;
    public static final int ACCOUNT_APPROVED_ID = 100;/* Account Approved (Send to Home)*/
    public static final int ORDER_ADDED_ID = 101;/* Order Added (Send to current order)*/
    public static final int DRIVER_ACCEPT_ID = 101;/*Driver Accept (Send to current Order)*/
    public static final int DRIVER_PICKUP_ID = 101;/*Driver Picked up the product (Send to current Order)*/
    public static final int DRIVER_DELIVERED_ID = 102;/*Delivered the order successfully (Send to Order History)*/

    public static final String SHARED_PREF = "ah_firebase";
    public static final String LOGOUT_RECEIVER = "logout_receiver";
    public static final String UPDATE_RECEIVER = "update_receiver";
}
