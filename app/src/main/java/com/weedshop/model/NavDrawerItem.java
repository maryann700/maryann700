package com.weedshop.model;

public class NavDrawerItem {

    private String title;
    private int id;

    public NavDrawerItem() {
    }

    public NavDrawerItem(String title, int id) {
        this.title = title;
        this.id = id;

    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}