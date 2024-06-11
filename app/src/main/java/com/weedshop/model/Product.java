package com.weedshop.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by MTPC-40 on 26-Apr-17.
 */

public class Product implements Serializable {
    public String id;
    public String storeId;
    public String name;
    public String categoryId;
    public String typeId;
    public String image;
    public String price;
    public String quantity;
    public String description;
    public String status;
    public String createdDate;
    public String category;
    public String type;
    public String imageUrl;
    public List<Attribute> attributes = null;

    public static class Attribute implements Serializable {
        public String name;
        public String attributeText;
        public String attributeId;
    }
}
