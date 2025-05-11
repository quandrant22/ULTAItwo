package com.example.ultai.api;

import android.content.Context;
// import android.content.SharedPreferences; // УДАЛЕНО
import android.util.Log;
import androidx.annotation.NonNull; // Для Firebase

// import com.example.ultai.api.models.AnswerRequest; // Если не используются в других методах
// import com.example.ultai.api.models.CompanyRequest; // Если не используются в других методах
// import com.example.ultai.api.models.LoginRequest; // УДАЛЕНО
// import com.example.ultai.api.models.RegisterRequest; // УДАЛЕНО
import com.example.ultai.api.models.UserResponse; // Оставляем для getUserData (пока)

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.HashMap;

import com.example.ultai.models.BasicQuestionnaire; // Если используется

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult; // Для Firebase
import com.google.android.gms.tasks.OnCompleteListener; // Для Firebase
import com.google.android.gms.tasks.Task; // Для Firebase


public class ApiClient {
    private static final String TAG = "ApiClient";
    // Удаляем константы для локального хранения
    // private static final String PREF_NAME = "UltaiPrefs";
    // private static final String TOKEN_KEY = "token";
    // private static final String USER_ID_KEY = "userId";
    // private static final String USERNAME_KEY = "username";

    // --- Остаются OkHttp константы, если используются для ДРУГИХ запросов ---
    private static final String BASE_URL = "http://127.0.0.1:3000/api"; // Оставляем, если есть другие запросы
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    // ------------------------------------------------------------------------

    private static ApiClient instance;
    private final OkHttpClient client; // Оставляем, если есть другие запросы
    private final Context context;
    private final FirebaseAuth mAuth; // Экземпляр Firebase Auth

    // Обработчик и исполнитель могут остаться, если нужны для других запросов
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);


    private ApiClient(Context context) {
        this.context = context.getApplicationContext();
        this.mAuth = FirebaseAuth.getInstance(); // Инициализируем Firebase Auth
        // Инициализация OkHttpClient остается, если он нужен для других запросов
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient не инициализирован. Сначала вызовите getInstance(Context)");
        }
        return instance;
    }

    // --- Удалены методы saveAuthDataInternal, saveAuthData, saveAuthDataToFile ---

    // --- Метод getToken() теперь не актуален или должен возвращать Firebase ID Token ---
    // Вернем null, предполагая, что ID Token не используется напрямую здесь
    public String getToken() {
         Log.d(TAG, "getToken() called, returning null (relying on FirebaseAuth state)");
         // Если нужен ID токен:
         /*
         FirebaseUser user = mAuth.getCurrentUser();
         if (user != null) {
             // Асинхронное получение токена! Этот метод не может быть синхронным.
             // user.getIdToken(true).addOnCompleteListener(...)
             return "Needs async implementation"; // Заглушка
         }
         */
            return null;
    }

    // --- Метод getUserId() теперь возвращает Firebase UID ---
    public String getUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = (user != null) ? user.getUid() : null;
        Log.d(TAG, "getUserId: returning Firebase UID: " + uid);
        return uid;
    }

    // --- Метод getUsername() теперь возвращает Firebase Display Name ---
    public String getUsername() {
        FirebaseUser user = mAuth.getCurrentUser();
        String displayName = (user != null) ? user.getDisplayName() : null;
         Log.d(TAG, "getUsername: returning Firebase Display Name: " + displayName);
        // Может быть null, если имя не установлено
        return displayName;
    }

    // --- isAuthenticated() теперь проверяет только Firebase ---
    public boolean isAuthenticated() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean authenticated = currentUser != null;
        Log.d(TAG, "isAuthenticated check: hasFirebaseUser=" + authenticated);
        return authenticated;
    }

    // --- logout() теперь только вызывает Firebase signOut ---
    public void logout() {
        try {
            Log.d(TAG, "logout: Calling Firebase signOut()...");
            mAuth.signOut();
            Log.d(TAG, "logout: Firebase signOut() completed.");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при выполнении Firebase signOut()", e);
        }
    }

    // --- Удалена синхронная версия register() ---

    // РЕАЛЬНАЯ РЕГИСТРАЦИЯ через Firebase Auth
    public void register(String username, String email, String password, String gender, String phone, ApiResponseCallback callback) {
        Log.d(TAG, "Attempting Firebase registration for: " + email);
        // Вызываем logout на всякий случай, чтобы очистить любое предыдущее состояние
        logout();

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(executorService, task -> { // Выполняем listener в фоновом потоке
                if (task.isSuccessful()) {
                    // Регистрация успешна
                    FirebaseUser user = mAuth.getCurrentUser();
                    Log.d(TAG, "Firebase registration successful. User UID: " + (user != null ? user.getUid() : "null"));
                    // TODO: Здесь можно сохранить доп. информацию (username, gender, phone)
                    // в Firebase Firestore или Realtime Database, привязанную к user.getUid()
                    // TODO: Можно также обновить FirebaseUser Profile (displayName)
                    // user.updateProfile(...)

                    // Вызываем onSuccess в главном потоке
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                    // Ошибка регистрации
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown registration error";
                    Log.e(TAG, "Firebase registration failed: " + errorMessage);
                    // Вызываем onError в главном потоке
                    mainHandler.post(() -> callback.onError("Ошибка регистрации: " + errorMessage));
            }
        });
    }

     // --- Удалена синхронная версия login() ---

    // РЕАЛЬНЫЙ ВХОД через Firebase Auth
    public void login(String email, String password, ApiResponseCallback callback) {
        Log.d(TAG, "Attempting Firebase login for: " + email);
        // Вызываем logout на всякий случай, чтобы очистить любое предыдущее состояние
        logout();

        mAuth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(executorService, task -> { // Выполняем listener в фоновом потоке
                 if (task.isSuccessful()) {
                     // Вход успешен
                     FirebaseUser user = mAuth.getCurrentUser();
                     Log.d(TAG, "Firebase login successful. User UID: " + (user != null ? user.getUid() : "null"));
                     // Вызываем onSuccess в главном потоке
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                     // Ошибка входа
                     String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown login error";
                     Log.e(TAG, "Firebase login failed: " + errorMessage);
                     // Вызываем onError в главном потоке
                     mainHandler.post(() -> callback.onError("Ошибка входа: " + errorMessage));
            }
        });
    }

    // ПОЛУЧЕНИЕ ДАННЫХ ПОЛЬЗОВАТЕЛЯ - ТРЕБУЕТ РЕАЛИЗАЦИИ С БЭКЕНДОМ/FIRESTORE
    public ApiResponse<UserResponse> getUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return new ApiResponse<>(false, "Пользователь не авторизован (Firebase)", null);
        }
        String userId = user.getUid();
        Log.d(TAG, "getUserData: Firebase User ID: " + userId);

        // ЗАГЛУШКА - здесь должен быть код для получения данных из вашего источника
        // Например, из Firestore по userId или запрос к вашему API с ID Token
        Log.e(TAG, "getUserData: МЕТОД ТРЕБУЕТ РЕАЛИЗАЦИИ для получения данных пользователя (Firestore/Backend)!");
        return new ApiResponse<>(false, "Метод получения данных пользователя не реализован", null);

        /* Примерный код для Firestore (нужна зависимость и настройка):
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Преобразовать document.getData() в UserResponse
                        // callback.onSuccess(userResponse);
            } else {
                        // callback.onError("Профиль пользователя не найден");
                    }
            } else {
                    // callback.onError("Ошибка получения профиля: " + task.getException());
                }
            });
        */
    }


    // --- Остальные методы (updateCompany, saveAnswers, getQuestions и т.д.) ---
    // --- остаются без изменений, НО им может потребоваться Firebase ID Token ---
    // --- для аутентификации запросов к вашему API, если он есть ---

    // Пример: добавление ID Token в заголовок (если OkHttp используется для других запросов)
    private void addAuthorizationHeader(Request.Builder builder, String idToken) {
         if (idToken != null) {
             builder.header("Authorization", "Bearer " + idToken);
         }
    }

    // Получение сообщения об ошибке из тела ответа (остается для других запросов)
    private String getErrorMessage(String errorBody) {
        try {
            JSONObject jsonObject = new JSONObject(errorBody);
            if (jsonObject.has("message")) {
                return jsonObject.getString("message");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Ошибка при парсинге ответа об ошибке", e);
        }
        return "Неизвестная ошибка";
    }

    /**
     * Интерфейс обратного вызова для асинхронных API-запросов
     */
    public interface ApiResponseCallback {
        void onSuccess(Object response);
        void onError(String errorMessage);
    }
    
    // --- saveBasicQuestionnaire, getBasicQuestionnaire, updateProfile ---
    // ЭТИ МЕТОДЫ ТОЖЕ, ВЕРОЯТНО, НУЖНО ПЕРЕДЕЛАТЬ
    // для работы с Firestore/RealtimeDB ИЛИ передавать Firebase ID Token вашему API

    public void saveBasicQuestionnaire(BasicQuestionnaire questionnaire, ApiResponseCallback callback) {
        // ЗАГЛУШКА - Требует реализации с Firestore/Backend
         Log.e(TAG, "saveBasicQuestionnaire: МЕТОД ТРЕБУЕТ РЕАЛИЗАЦИИ (Firestore/Backend)!");
         mainHandler.post(() -> callback.onError("Метод saveBasicQuestionnaire не реализован"));
        // ... старый код ...
    }

    public void getBasicQuestionnaire(ApiResponseCallback callback) {
         // ЗАГЛУШКА - Требует реализации с Firestore/Backend
         Log.e(TAG, "getBasicQuestionnaire: МЕТОД ТРЕБУЕТ РЕАЛИЗАЦИИ (Firestore/Backend)!");
         mainHandler.post(() -> callback.onError("Метод getBasicQuestionnaire не реализован"));
        // ... старый код ...
    }

    public void updateProfile(String name, String email, String phone, String gender, String role, String companyName, ApiResponseCallback callback) {
         // ЗАГЛУШКА - Требует реализации с Firestore/Backend
         Log.e(TAG, "updateProfile: МЕТОД ТРЕБУЕТ РЕАЛИЗАЦИИ (Firestore/Backend)!");
         mainHandler.post(() -> callback.onError("Метод updateProfile не реализован"));
       // ... старый код ...
    }

    // --- Удалены внутренние методы для работы с локальным хранением ---
    // private void saveAuthDataToFile(...)
    // private String tryRestoreTokenFromFile(...)

    // --- Оставляем makeRequest, если он используется для ДРУГИХ API запросов ---
    private enum RequestMethod { GET, POST, PUT, DELETE }
    private void makeRequest(String endpoint, RequestMethod method, JSONObject data, ApiResponseCallback callback) {
         // Этот метод теперь, вероятно, не нужен для auth, но может быть нужен для других запросов
         // Если используется, ему нужно добавить получение Firebase ID Token и передачу в заголовке
         Log.w(TAG, "makeRequest: Этот метод, вероятно, устарел или требует добавления Firebase Auth Token");
        // Временная имитация запроса к серверу
        executorService.execute(() -> {
            try {
                Thread.sleep(1000);
                mainHandler.post(() -> callback.onSuccess("Запрос успешно выполнен (ЗАГЛУШКА)"));
            } catch (InterruptedException e) {
                mainHandler.post(() -> callback.onError("Ошибка выполнения запроса: " + e.getMessage()));
            }
        });
    }
}

