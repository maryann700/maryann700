package com.weedshop.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by MTPC-40 on 24-Apr-17.
 */

public class Address implements Serializable {

    public String id;
    public String user_id;
    public String firstname;
    public String lastname;
    public String address;
    //  public String street;
    public String region;
    public String city;
    public String phone;
    public String zipcode;
    public String country;
    public String state;
    public String latitude;
    public String longitude;
    public String status;
}
