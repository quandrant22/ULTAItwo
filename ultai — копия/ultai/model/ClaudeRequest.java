package com.example.ultai.ultai.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Класс для формирования запросов к API Claude от Anthropic
 * Документация: https://docs.anthropic.com/claude/reference/messages_post
 */
public class ClaudeRequest {
    @SerializedName("model")
    private String model;
    
    @SerializedName("messages")
    private List<Message> messages;
    
    @SerializedName("max_tokens")
    private int maxTokens;
    
    @SerializedName("temperature")
    private double temperature;
    
    @SerializedName("top_p")
    private double topP;
    
    @SerializedName("top_k")
    private Integer topK;
    
    @SerializedName("stream")
    private boolean stream;
    
    @SerializedName("system")
    private String system;

    /**
     * Конструктор с основными параметрами
     */
    public ClaudeRequest(String model, List<Message> messages, String system) {
        this.model = model;
        this.messages = messages;
        this.system = system;
        this.maxTokens = 4000;
        this.temperature = 0.7;
        this.topP = 0.95;
        this.stream = false;
    }

    /**
     * Класс для представления сообщения в формате Claude API
     */
    public static class Message {
        @SerializedName("role")
        private String role;
        
        @SerializedName("content")
        private List<Content> content;

        public Message(String role, String text) {
            this.role = role;
            this.content = List.of(new Content("text", text));
        }

        public String getRole() {
            return role;
        }

        public List<Content> getContent() {
            return content;
        }
    }

    /**
     * Класс для представления содержимого сообщения
     */
    public static class Content {
        @SerializedName("type")
        private String type;
        
        @SerializedName("text")
        private String text;

        public Content(String type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    // Геттеры и сеттеры
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTopP() {
        return topP;
    }

    public void setTopP(double topP) {
        this.topP = topP;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }
} 