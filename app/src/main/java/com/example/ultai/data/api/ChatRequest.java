package com.example.ultai.data.api;

import java.util.List;

public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private double temperature;

    public ChatRequest(List<ChatMessage> messages) {
        this.model = "deepseek-chat";
        this.messages = messages;
        this.temperature = 0.7;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
} 