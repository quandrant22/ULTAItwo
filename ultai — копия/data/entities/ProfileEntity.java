package com.example.ultai.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Сущность профиля пользователя для хранения в базе данных
 */
@Entity(
    tableName = "profiles",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "userId", unique = true)
    }
)
public class ProfileEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private String role;
    private String companyName;
    private boolean synced;
    
    // Конструктор по умолчанию
    public ProfileEntity() {
    }
    
    // Конструктор с параметрами
    public ProfileEntity(int userId, String name, String email, String phone, String gender, String role, String companyName) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
        this.companyName = companyName;
        this.synced = false;
    }
    
    // Геттеры и сеттеры
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public boolean isSynced() {
        return synced;
    }
    
    public void setSynced(boolean synced) {
        this.synced = synced;
    }
} 