package com.example.ultai.data.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Сущность пользователя для хранения в базе данных
 */
@Entity(
    tableName = "users",
    indices = {
        @Index(value = "email", unique = true)
    }
)
public class UserEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String username;
    private String email;
    private String password;
    private String gender;
    private String phone;
    private boolean active;
    private String token;
    private boolean synced;
    
    // Конструктор по умолчанию
    public UserEntity() {
    }
    
    // Конструктор с параметрами
    @androidx.room.Ignore
    public UserEntity(String username, String email, String password, String gender, String phone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.phone = phone;
        this.active = true;
        this.synced = false;
    }
    
    // Геттеры и сеттеры
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public boolean isSynced() {
        return synced;
    }
    
    public void setSynced(boolean synced) {
        this.synced = synced;
    }
} 
