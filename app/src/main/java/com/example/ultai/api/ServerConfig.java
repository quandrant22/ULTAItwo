package com.example.ultai.api;

/**
 * Класс конфигурации для соединения с внешним сервером.
 * Содержит настройки и константы для взаимодействия с API.
 */
public class ServerConfig {
    // URL внешнего сервера API
    public static final String BASE_URL = "http://10.0.2.2:8080/api"; // Для эмулятора
    // public static final String BASE_URL = "http://192.168.1.100:8080/api"; // Для реального устройства (изменить IP)
    
    // Таймауты для сетевых запросов (в секундах)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
    
    // Настройки авторизации
    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_PREFIX = "Bearer ";
    
    // Эндпоинты API
    public static final String ENDPOINT_REGISTER = "/register";
    public static final String ENDPOINT_LOGIN = "/login";
    public static final String ENDPOINT_USER = "/user";
    public static final String ENDPOINT_PROFILE = "/profile";
    public static final String ENDPOINT_QUESTIONNAIRE = "/questionnaire";
    
    // Настройки для хранения данных авторизации
    public static final String PREF_NAME = "UltaiPrefs";
    public static final String TOKEN_KEY = "token";
    public static final String USER_ID_KEY = "userId";
    public static final String USERNAME_KEY = "username";
    
    // Настройки синхронизации
    public static final long SYNC_INTERVAL_MILLIS = 30 * 60 * 1000; // 30 минут
    public static final boolean AUTO_SYNC_ENABLED = true;
} 