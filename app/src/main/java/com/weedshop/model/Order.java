package com.weedshop.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by MTPC-40 on 24-May-17.
 */

public class Order implements Serializable {

    public String id;
    public String orderCode;
    public String userId;
    public String driverId;
    public String storeId;
    public String orderDate;
    public String finalTotal;
    public String status;
    public String totalProducts;
    public ArrayList<Product> products = null;

    public static class Product {
        public String orderId;
        public String productId;
        public String productName;
        public String price;
        public String quantity;
        public String attributeDescription;
        public String imageUrl;
        public String type;
        public String color;
    }
}
