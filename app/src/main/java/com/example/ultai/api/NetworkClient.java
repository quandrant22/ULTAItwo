package com.example.ultai.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Клиент для взаимодействия с внешним сервером
 */
public class NetworkClient {
    private static final String TAG = "NetworkClient";
    private static final String BASE_URL = "http://10.0.2.2:3000/api"; // Базовый URL внешнего сервера
    private static final String PREF_NAME = "NetworkPrefs";
    private static final String TOKEN_KEY = "remote_token";
    private static final String USER_ID_KEY = "remote_userId";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static NetworkClient instance;
    private final OkHttpClient client;
    private final Context context;
    private final Handler mainHandler;
    private final ExecutorService executorService;
    private final SharedPreferences preferences;

    private NetworkClient(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newFixedThreadPool(4);
    }

    /**
     * Получение экземпляра клиента (Singleton)
     */
    public static synchronized NetworkClient getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkClient(context);
        }
        return instance;
    }

    /**
     * Сохранение токена и ID пользователя
     */
    private void saveAuthData(String token, String userId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.putString(USER_ID_KEY, userId);
        editor.apply();
    }

    /**
     * Получение токена
     */
    public String getToken() {
        return preferences.getString(TOKEN_KEY, null);
    }

    /**
     * Получение ID пользователя
     */
    public String getUserId() {
        return preferences.getString(USER_ID_KEY, null);
    }

    /**
     * Проверка авторизации
     */
    public boolean isAuthenticated() {
        return getToken() != null;
    }

    /**
     * Выход (удаление данных авторизации)
     */
    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(TOKEN_KEY);
        editor.remove(USER_ID_KEY);
        editor.apply();
    }

    /**
     * Регистрация пользователя
     */
    public void register(String username, String email, String password, String gender, String phone, 
                       NetworkCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("username", username);
                jsonRequest.put("email", email);
                jsonRequest.put("password", password);
                jsonRequest.put("gender", gender);
                jsonRequest.put("phone", phone);

                RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);
                Request request = new Request.Builder()
                        .url(BASE_URL + "/register")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    
                    if (jsonResponse.getBoolean("success")) {
                        String token = jsonResponse.getString("token");
                        String userId = jsonResponse.getString("userId");
                        saveAuthData(token, userId);
                        
                        mainHandler.post(() -> callback.onSuccess(null));
                    } else {
                        String message = jsonResponse.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Ошибка сервера: " + response.code()));
                }
            } catch (IOException | JSONException e) {
                mainHandler.post(() -> callback.onError("Ошибка сети: " + e.getMessage()));
            }
        });
    }

    /**
     * Вход пользователя
     */
    public void login(String email, String password, NetworkCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("email", email);
                jsonRequest.put("password", password);

                RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);
                Request request = new Request.Builder()
                        .url(BASE_URL + "/login")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    
                    if (jsonResponse.getBoolean("success")) {
                        String token = jsonResponse.getString("token");
                        String userId = jsonResponse.getString("userId");
                        saveAuthData(token, userId);
                        
                        mainHandler.post(() -> callback.onSuccess(null));
                    } else {
                        String message = jsonResponse.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Ошибка сервера: " + response.code()));
                }
            } catch (IOException | JSONException e) {
                mainHandler.post(() -> callback.onError("Ошибка сети: " + e.getMessage()));
            }
        });
    }

    /**
     * Обновление профиля пользователя
     */
    public void updateProfile(String name, String email, String phone, String gender, String role, 
                           String companyName, NetworkCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                String token = getToken();
                if (token == null) {
                    mainHandler.post(() -> callback.onError("Пользователь не авторизован"));
                    return;
                }

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("name", name);
                jsonRequest.put("email", email);
                jsonRequest.put("phone", phone);
                jsonRequest.put("gender", gender);
                jsonRequest.put("role", role);
                jsonRequest.put("companyName", companyName);

                RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);
                Request request = new Request.Builder()
                        .url(BASE_URL + "/profile")
                        .header("Authorization", "Bearer " + token)
                        .put(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    
                    if (jsonResponse.getBoolean("success")) {
                        mainHandler.post(() -> callback.onSuccess(null));
                    } else {
                        String message = jsonResponse.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Ошибка сервера: " + response.code()));
                }
            } catch (IOException | JSONException e) {
                mainHandler.post(() -> callback.onError("Ошибка сети: " + e.getMessage()));
            }
        });
    }

    /**
     * Сохранение анкеты на внешнем сервере
     */
    public void saveQuestionnaire(JSONObject questionnaireData, final NetworkCallback<String> callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Пользователь не авторизован");
            return;
        }
        
        RequestBody body = RequestBody.create(JSON, questionnaireData.toString());
        Request request = new Request.Builder()
                .url(BASE_URL + "/questionnaires")
                .header("Authorization", "Bearer " + token)
                .post(body)
                .build();
        
        executeRequest(request, new NetworkResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                callback.onSuccess("Анкета успешно сохранена");
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError("Ошибка при сохранении анкеты: " + errorMessage);
            }
        });
    }

    /**
     * Получение профиля пользователя
     */
    public void getProfile(NetworkCallback<JSONObject> callback) {
        executorService.execute(() -> {
            try {
                String token = getToken();
                if (token == null) {
                    mainHandler.post(() -> callback.onError("Пользователь не авторизован"));
                    return;
                }

                Request request = new Request.Builder()
                        .url(BASE_URL + "/profile")
                        .header("Authorization", "Bearer " + token)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    
                    if (jsonResponse.getBoolean("success")) {
                        JSONObject profile = jsonResponse.getJSONObject("profile");
                        mainHandler.post(() -> callback.onSuccess(profile));
                    } else {
                        String message = jsonResponse.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Ошибка сервера: " + response.code()));
                }
            } catch (IOException | JSONException e) {
                mainHandler.post(() -> callback.onError("Ошибка сети: " + e.getMessage()));
            }
        });
    }

    /**
     * Получение анкеты пользователя
     */
    public void getQuestionnaire(NetworkCallback<JSONObject> callback) {
        executorService.execute(() -> {
            try {
                String token = getToken();
                if (token == null) {
                    mainHandler.post(() -> callback.onError("Пользователь не авторизован"));
                    return;
                }

                Request request = new Request.Builder()
                        .url(BASE_URL + "/questionnaire")
                        .header("Authorization", "Bearer " + token)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    
                    if (jsonResponse.getBoolean("success")) {
                        JSONObject questionnaire = jsonResponse.getJSONObject("questionnaire");
                        mainHandler.post(() -> callback.onSuccess(questionnaire));
                    } else {
                        String message = jsonResponse.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Ошибка сервера: " + response.code()));
                }
            } catch (IOException | JSONException e) {
                mainHandler.post(() -> callback.onError("Ошибка сети: " + e.getMessage()));
            }
        });
    }

    /**
     * Интерфейс обратного вызова для асинхронных запросов
     */
    public interface NetworkCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
    
    /**
     * Интерфейс обратного вызова для обработки ответов сервера
     */
    public interface NetworkResponseCallback {
        void onSuccess(JSONObject response);
        void onError(String errorMessage);
    }
    
    /**
     * Выполнение запроса с обработкой ответа
     */
    private void executeRequest(Request request, NetworkResponseCallback callback) {
        executorService.execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    
                    if (jsonResponse.optBoolean("success", false)) {
                        mainHandler.post(() -> callback.onSuccess(jsonResponse));
                    } else {
                        String message = jsonResponse.optString("message", "Неизвестная ошибка");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Ошибка сервера: " + response.code()));
                }
            } catch (IOException | JSONException e) {
                mainHandler.post(() -> callback.onError("Ошибка сети: " + e.getMessage()));
            }
        });
    }

    /**
     * Выполнение асинхронного запроса
     */
    private void executeAsync(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }
} 