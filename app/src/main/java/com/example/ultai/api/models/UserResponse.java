package com.example.ultai.api.models;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserResponse {
    private final User user;
    private final Company company;
    private final Map<String, String> answers;

    public UserResponse(User user, Company company, Map<String, String> answers) {
        this.user = user;
        this.company = company;
        this.answers = answers;
    }

    public User getUser() {
        return user;
    }

    public Company getCompany() {
        return company;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public static UserResponse fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        
        // Парсинг пользователя
        JSONObject userJson = jsonObject.getJSONObject("user");
        User user = User.fromJson(userJson);
        
        // Парсинг компании
        Company company = null;
        if (!jsonObject.isNull("company")) {
            JSONObject companyJson = jsonObject.getJSONObject("company");
            company = Company.fromJson(companyJson);
        }
        
        // Парсинг ответов
        Map<String, String> answers = new HashMap<>();
        if (!jsonObject.isNull("answers")) {
            JSONObject answersJson = jsonObject.getJSONObject("answers");
            Iterator<String> keys = answersJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.startsWith("Ответ ")) {
                    answers.put(key.substring(6), answersJson.getString(key));
                }
            }
        }
        
        return new UserResponse(user, company, answers);
    }

    public static class User {
        private final String id;
        private final String username;
        private final String email;
        private final String gender;
        private final String phone;
        private final String phase;
        private final String stage;
        private final String step;

        public User(String id, String username, String email, String gender, String phone, String phase, String stage, String step) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.gender = gender;
            this.phone = phone;
            this.phase = phase;
            this.stage = stage;
            this.step = step;
        }

        public String getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getGender() {
            return gender;
        }

        public String getPhone() {
            return phone;
        }

        public String getPhase() {
            return phase;
        }

        public String getStage() {
            return stage;
        }

        public String getStep() {
            return step;
        }

        public static User fromJson(JSONObject json) throws JSONException {
            return new User(
                    json.getString("id"),
                    json.getString("username"),
                    json.optString("email", ""),
                    json.optString("gender", "не указан"),
                    json.optString("phone", ""),
                    json.optString("phase", "1"),
                    json.optString("stage", "1"),
                    json.optString("step", "1")
            );
        }
    }

    public static class Company {
        private final String name;
        private final String country;
        private final String type;
        private final String products;
        private final String geography;
        private final String city;

        public Company(String name, String country, String type, String products, String geography, String city) {
            this.name = name;
            this.country = country;
            this.type = type;
            this.products = products;
            this.geography = geography;
            this.city = city;
        }

        public String getName() {
            return name;
        }

        public String getCountry() {
            return country;
        }

        public String getType() {
            return type;
        }

        public String getProducts() {
            return products;
        }

        public String getGeography() {
            return geography;
        }

        public String getCity() {
            return city;
        }

        public static Company fromJson(JSONObject json) throws JSONException {
            return new Company(
                    json.getString("Company Name"),
                    json.getString("Country"),
                    json.getString("Type"),
                    json.getString("Products"),
                    json.getString("Geography"),
                    json.getString("City")
            );
        }
    }
} 