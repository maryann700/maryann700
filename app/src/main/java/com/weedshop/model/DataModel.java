package com.weedshop.model;

/**
 * Created by MTPC-110 on 5/24/2017.
 */

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pratap.kesaboyina on 30-11-2015.
 */
public class DataModel {
    private String headerTitle;
    private ArrayList<HashMap<String, String>> allItemsInSection;

    public DataModel() {
    }

    public DataModel(String headerTitle, ArrayList<HashMap<String, String>> allItemsInSection) {
        this.headerTitle = headerTitle;
        this.allItemsInSection = allItemsInSection;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public ArrayList<HashMap<String, String>> getAllItemsInSection() {
        return allItemsInSection;
    }

    public void setAllItemsInSection(ArrayList<HashMap<String, String>> allItemsInSection) {
        this.allItemsInSection = allItemsInSection;
    }
}