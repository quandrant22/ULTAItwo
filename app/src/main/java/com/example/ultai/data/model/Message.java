package com.example.ultai.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String content;
    private boolean isUser;
    private Date timestamp;

    public Message(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
} 