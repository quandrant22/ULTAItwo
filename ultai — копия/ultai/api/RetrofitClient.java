package com.example.ultai.ultai.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Interceptor;
import android.util.Log;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;
import okhttp3.Response;
import okhttp3.ConnectionPool;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "https://api.deepseek.com/v1/";
    // Токен может иметь срок действия. Обновите его при необходимости
    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private String apiKey;

    private RetrofitClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();
                    
                    if (apiKey != null && !apiKey.isEmpty()) {
                        builder.header("Authorization", "Bearer " + apiKey);
                        Log.d(TAG, "Добавлен заголовок авторизации с ключом: " + apiKey.substring(0, 8) + "...");
                    } else {
                        Log.e(TAG, "API ключ не установлен!");
                    }
                    
                    builder.header("Content-Type", "application/json")
                           .header("Accept", "application/json")
                           .header("User-Agent", "Ultai-Android/1.0");
                    
                    Request request = builder.build();
                    
                    Log.d(TAG, "Отправка запроса к: " + request.url());
                    
                    Response response = chain.proceed(request);
                    
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Ошибка сервера: " + response.code());
                    }
                    
                    return response;
                })
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS))
                .retryOnConnectionFailure(true)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        Log.d(TAG, "API ключ установлен: " + apiKey.substring(0, 8) + "...");
    }

    public <T> T create(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public String getHeaders() {
        return "Authorization: Bearer " + (apiKey != null ? apiKey.substring(0, 8) + "..." : "не установлен");
    }
} 