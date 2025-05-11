package com.example.ultai.ultai.model;

import java.util.ArrayList;
import java.util.List;

public class DeepSeekRequest {
    private String model = "deepseek-chat";
    private List<Message> messages;
    private boolean stream = false;

    public DeepSeekRequest(String userMessage) {
        this.messages = new ArrayList<>();
        this.messages.add(new Message("system", "You are a helpful assistant."));
        this.messages.add(new Message("user", userMessage));
    }

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

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}