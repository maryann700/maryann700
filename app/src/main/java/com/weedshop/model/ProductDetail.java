package com.weedshop.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by MTPC-40 on 26-Apr-17.
 */

public class ProductDetail implements Serializable {
    public String id;
    public String storeId;
    public String name;
    public String typeId;
    public String image;
    public String price;
    public String quantity;
    public String description;
    public String type;
    public String color;
    public String delivery_charge;
    public String main_image_url;
    public List<Attribute> attributes = null;
    public List<Images> images = null;

    public static class Attribute implements Serializable {
        public String name;
        public String attributeText;
        public String attributeId;
    }

    public static class Images implements Serializable {
        public String image;
        public String image_url;
        public String product_id;
    }
}
