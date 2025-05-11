package com.example.ultai.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String gender;

    // Конструктор без пола
    public User(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.gender = "не указан";
    }
    
    // Конструктор с полом
    public User(String name, String email, String phone, String password, String gender) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.gender = gender;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
