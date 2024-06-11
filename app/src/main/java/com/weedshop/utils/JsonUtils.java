package com.weedshop.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class JsonUtils {

    public static String getJsonArrayString(ArrayList<Map<String, Object>> customList) {
        Gson gson = new GsonBuilder().create();

        return gson.toJsonTree(customList).getAsJsonArray().toString();
    }

    public static String getJsonArrayString(Map<String, Object> map) {

        Type type = new TypeToken<JsonArray>() {
        }.getType();

        // Convert a Map into JSON string.
        Gson gson = new Gson();
        String json = gson.toJson(map);
        return json;
    }
}