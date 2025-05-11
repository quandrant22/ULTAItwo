package com.example.ultai.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.ultai.data.converters.StringArrayConverter;

/**
 * Сущность анкеты для хранения в базе данных
 */
@Entity(
    tableName = "questionnaires",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "userId")
    }
)
public class QuestionnaireEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int userId;
    private String companyName;
    private String country;
    private String city;
    private String activityType;
    private String productsServicesDescription;
    private String marketScope;
    private String businessState;
    
    private String targetCountry;
    
    @TypeConverters(StringArrayConverter.class)
    private String[] targetCountries;
    
    private boolean synced;
    
    // Конструктор по умолчанию
    public QuestionnaireEntity() {
    }
    
    // Конструктор с параметрами
    public QuestionnaireEntity(int userId, String companyName, String country, String activityType, 
                             String productsServicesDescription, String marketScope, String businessState) {
        this.userId = userId;
        this.companyName = companyName;
        this.country = country;
        this.activityType = activityType;
        this.productsServicesDescription = productsServicesDescription;
        this.marketScope = marketScope;
        this.businessState = businessState;
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
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
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
    
    public boolean isSynced() {
        return synced;
    }
    
    public void setSynced(boolean synced) {
        this.synced = synced;
    }
} 