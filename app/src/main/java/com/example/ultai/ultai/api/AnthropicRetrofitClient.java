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

/**
 * Клиент Retrofit для работы с API Anthropic (Claude)
 */
public class AnthropicRetrofitClient {
    private static final String TAG = "AnthropicRetrofitClient";
    private static final String BASE_URL = "https://api.anthropic.com/";
    private static AnthropicRetrofitClient instance;
    private final Retrofit retrofit;
    private String apiKey;
    private String anthropicVersion = "2023-06-01"; // Версия API Anthropic

    private AnthropicRetrofitClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();
                    
                    if (apiKey != null && !apiKey.isEmpty()) {
                        builder.header("x-api-key", apiKey);
                        builder.header("anthropic-version", anthropicVersion);
                        Log.d(TAG, "Добавлен заголовок x-api-key с ключом: " + apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
                    } else {
                        Log.e(TAG, "API ключ Anthropic не установлен!");
                    }
                    
                    builder.header("Content-Type", "application/json")
                           .header("Accept", "application/json")
                           .header("User-Agent", "Ultai-Android/1.0");
                    
                    Request request = builder.build();
                    
                    Log.d(TAG, "Отправка запроса к Anthropic API: " + request.url());
                    
                    Response response = chain.proceed(request);
                    
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Ошибка сервера Anthropic: " + response.code());
                        try {
                            String errorBody = response.body() != null ? response.body().string() : "Нет тела ответа";
                            Log.e(TAG, "Тело ошибки: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Не удалось прочитать тело ошибки", e);
                        }
                    }
                    
                    return response;
                })
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS))
                .retryOnConnectionFailure(true)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized AnthropicRetrofitClient getInstance() {
        if (instance == null) {
            instance = new AnthropicRetrofitClient();
        }
        return instance;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        Log.d(TAG, "API ключ Anthropic установлен: " + apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
    }
    
    public void setAnthropicVersion(String version) {
        this.anthropicVersion = version;
        Log.d(TAG, "Версия API Anthropic установлена: " + version);
    }

    public <T> T create(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public String getBaseUrl() {
        return BASE_URL;
    }
} 