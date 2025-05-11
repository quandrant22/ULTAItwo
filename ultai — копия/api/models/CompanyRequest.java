package com.example.ultai.api.models;

import org.json.JSONException;
import org.json.JSONObject;

public class CompanyRequest {
    private final String companyName;
    private final String country;
    private final String type;
    private final String products;
    private final String geography;
    private final String city;

    public CompanyRequest(String companyName, String country, String type, String products, String geography, String city) {
        this.companyName = companyName;
        this.country = country;
        this.type = type;
        this.products = products;
        this.geography = geography;
        this.city = city;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("companyName", companyName);
            jsonObject.put("country", country);
            jsonObject.put("type", type);
            jsonObject.put("products", products);
            jsonObject.put("geography", geography);
            jsonObject.put("city", city);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
} 