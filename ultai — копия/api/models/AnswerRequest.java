package com.example.ultai.api.models;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

public class AnswerRequest {
    private final Map<String, String> answers;

    public AnswerRequest(Map<String, String> answers) {
        this.answers = answers;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        JSONObject answersObject = new JSONObject();

        try {
            for (Map.Entry<String, String> entry : answers.entrySet()) {
                answersObject.put(entry.getKey(), entry.getValue());
            }
            jsonObject.put("answers", answersObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }
} 