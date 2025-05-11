package com.example.ultai.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Класс для управления данными компании пользователя.
 * Обеспечивает сохранение и получение основных данных компании
 * между сеансами приложения через SharedPreferences.
 */
public class CompanyDataManager {
    private static final String PREFS_NAME = "company_data";
    private static final String KEY_COMPANY_NAME = "company_name";
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_ACTIVITY_TYPE = "activity_type";
    private static final String KEY_ACTIVITY_DESCRIPTION = "activity_description";
    private static final String DEFAULT_COMPANY_NAME = "Просто блеск";
    
    private static CompanyDataManager instance;
    private final SharedPreferences preferences;
    
    private CompanyDataManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Получение экземпляра менеджера (Singleton)
     */
    public static synchronized CompanyDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new CompanyDataManager(context);
        }
        return instance;
    }
    
    /**
     * Сохранение названия компании
     */
    public void saveCompanyName(String companyName) {
        preferences.edit().putString(KEY_COMPANY_NAME, companyName).apply();
    }
    
    /**
     * Сохранение названия компании с немедленным сохранением (commit)
     * @param companyName название компании
     * @return true если сохранение прошло успешно
     */
    public boolean saveCompanyNameCommit(String companyName) {
        return preferences.edit().putString(KEY_COMPANY_NAME, companyName).commit();
    }
    
    /**
     * Получение названия компании
     */
    public String getCompanyName() {
        return preferences.getString(KEY_COMPANY_NAME, DEFAULT_COMPANY_NAME);
    }
    
    /**
     * Сохранение страны компании
     */
    public void saveCountry(String country) {
        preferences.edit().putString(KEY_COUNTRY, country).apply();
    }
    
    /**
     * Получение страны компании
     */
    public String getCountry() {
        return preferences.getString(KEY_COUNTRY, "");
    }
    
    /**
     * Сохранение типа деятельности
     */
    public void saveActivityType(String activityType) {
        preferences.edit().putString(KEY_ACTIVITY_TYPE, activityType).apply();
    }
    
    /**
     * Получение типа деятельности
     */
    public String getActivityType() {
        return preferences.getString(KEY_ACTIVITY_TYPE, "");
    }
    
    /**
     * Сохранение описания деятельности
     */
    public void saveActivityDescription(String description) {
        preferences.edit().putString(KEY_ACTIVITY_DESCRIPTION, description).apply();
    }
    
    /**
     * Получение описания деятельности
     */
    public String getActivityDescription() {
        return preferences.getString(KEY_ACTIVITY_DESCRIPTION, "");
    }
    
    /**
     * Проверка, заполнены ли основные данные компании
     */
    public boolean hasCompanyData() {
        String companyName = getCompanyName();
        return !TextUtils.isEmpty(companyName) && !companyName.equals(DEFAULT_COMPANY_NAME);
    }
    
    /**
     * Очистка всех данных компании
     */
    public void clearCompanyData() {
        preferences.edit().clear().apply();
    }
} 