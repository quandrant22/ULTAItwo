package com.example.ultai.ultai.model;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private String time;
    private String moreInfo;

    public ChatMessage(String message, boolean isUser, String time, String moreInfo) {
        this.message = message;
        this.isUser = isUser;
        this.time = time;
        this.moreInfo = moreInfo;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getTime() {
        return time;
    }

    public String getMoreInfo() {
        return moreInfo;
    }
}