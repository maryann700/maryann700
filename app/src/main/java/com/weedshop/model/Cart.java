package com.weedshop.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by MTPC-40 on 26-Apr-17.
 */

public class Cart implements Serializable {
    public String storeId;
    public String storeName;
    public String productId;
    public String cartQuantity;
    public String totalQuantity;
    public String name;
    public String type;
    public String color;
    public String price;
    public String image;
    public String imageUrl;
    public String latitude;
    public String longitude;
    public List<Attribute> attributes = null;

    public static class Attribute implements Serializable {
        public String name;
        public String attributeText;
        public String attributeId;
    }

}
