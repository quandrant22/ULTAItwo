package com.example.ultai.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "basic_questionnaires")
public class BasicQuestionnaire {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int userId; // ID пользователя, к которому привязана анкета
    
    // Базовые данные анкеты
    private String companyName;
    private String country;
    private String activityType; // "Производство", "Товары", "Услуги"
    private String productsServicesDescription;
    private String marketScope; // "Локальная", "Национальная", "Международная"
    private String businessState; // "Планирую запустить", "Уже запущен"
    
    // Дополнительные поля в зависимости от выбора marketScope
    private String city; // для локальной реализации
    private String targetCountry; // для национальной и международной реализации
    private String[] targetCountries; // для международной реализации
    
    // Конструктор по умолчанию
    public BasicQuestionnaire() {
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getProductsServicesDescription() {
        return productsServicesDescription;
    }

    public void setProductsServicesDescription(String productsServicesDescription) {
        this.productsServicesDescription = productsServicesDescription;
    }

    public String getMarketScope() {
        return marketScope;
    }

    public void setMarketScope(String marketScope) {
        this.marketScope = marketScope;
    }

    public String getBusinessState() {
        return businessState;
    }

    public void setBusinessState(String businessState) {
        this.businessState = businessState;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTargetCountry() {
        return targetCountry;
    }

    public void setTargetCountry(String targetCountry) {
        this.targetCountry = targetCountry;
    }

    public String[] getTargetCountries() {
        return targetCountries;
    }

    public void setTargetCountries(String[] targetCountries) {
        this.targetCountries = targetCountries;
    }
} 