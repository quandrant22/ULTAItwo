package com.example.ultai.api.models;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterRequest {
    private final String username;
    private final String email;
    private final String password;
    private final String gender;
    private final String phone;

    public RegisterRequest(String username, String email, String password, String gender, String phone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.phone = phone;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("gender", gender);
            jsonObject.put("phone", phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
} 