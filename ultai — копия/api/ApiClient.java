package com.example.ultai.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.ultai.api.models.AnswerRequest;
import com.example.ultai.api.models.CompanyRequest;
import com.example.ultai.api.models.LoginRequest;
import com.example.ultai.api.models.RegisterRequest;
import com.example.ultai.api.models.UserResponse;

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

import com.example.ultai.models.BasicQuestionnaire;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://127.0.0.1:3000/api"; // Локальный хост для сервера
    // private static final String BASE_URL = "http://10.0.2.2:3000/api"; // Для эмулятора
    private static final String PREF_NAME = "UltaiPrefs";
    private static final String TOKEN_KEY = "token";
    private static final String USER_ID_KEY = "userId";
    private static final String USERNAME_KEY = "username";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static ApiClient instance;
    private final OkHttpClient client;
    private final Context context;

    // Обработчик для выполнения кода в главном потоке
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    // Исполнитель для асинхронных задач
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    // Флаг, указывающий на то, запущен ли сервер
    private static boolean isServerRunning = false;

    private ApiClient(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // Отключаем запуск локального сервера, так как он вызывает ошибку
        // startServerIfNeeded();
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    // Фабричный метод без контекста (использует последний установленный контекст)
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient не инициализирован. Сначала вызовите getInstance(Context)");
        }
        return instance;
    }

    // Метод для сохранения токена и данных пользователя в SharedPreferences
    private void saveAuthData(String token, String userId, String username) {
        try {
            // Используем более строгие имена для SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Очищаем перед сохранением
            editor.putString(TOKEN_KEY, token);
            editor.putString(USER_ID_KEY, userId);
            editor.putString(USERNAME_KEY, username);
            
            // Гарантированно синхронно сохраняем
            boolean success = editor.commit();
            
            // Проверяем, что данные действительно сохранились
            if (success) {
                // Дублируем информацию в дополнительном файле для надежности
                SharedPreferences backupPrefs = context.getSharedPreferences(PREF_NAME + "_backup", Context.MODE_PRIVATE);
                SharedPreferences.Editor backupEditor = backupPrefs.edit();
                backupEditor.putString(TOKEN_KEY, token);
                backupEditor.putString(USER_ID_KEY, userId);
                backupEditor.putString(USERNAME_KEY, username);
                backupEditor.commit();
                
                // Также сохраняем в файл
                saveAuthDataToFile(token, userId, username);
            }
            
            Log.d(TAG, "Сохранение данных аутентификации: " + 
                  "token=" + token + 
                  ", userId=" + userId + 
                  ", username=" + username + 
                  ", success=" + success);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении данных авторизации", e);
        }
    }

    // Метод для получения токена с проверкой резервной копии
    public String getToken() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String token = prefs.getString(TOKEN_KEY, null);
            
            // Если токен не найден, проверяем резервную копию
            if (token == null || token.isEmpty()) {
                SharedPreferences backupPrefs = context.getSharedPreferences(PREF_NAME + "_backup", Context.MODE_PRIVATE);
                token = backupPrefs.getString(TOKEN_KEY, null);
                
                // Если нашли в резервной копии, восстанавливаем основные данные
                if (token != null && !token.isEmpty()) {
                    Log.d(TAG, "Восстановление токена из резервной копии: " + token);
                    
                    // Восстанавливаем все данные из резервной копии
                    String userId = backupPrefs.getString(USER_ID_KEY, null);
                    String username = backupPrefs.getString(USERNAME_KEY, null);
                    
                    if (userId != null && username != null) {
                        saveAuthData(token, userId, username);
                    }
                } else {
                    // Если и в резервной копии нет, проверяем в файле
                    tryRestoreAuthDataFromFile();
                    // После восстановления проверяем еще раз
                    token = prefs.getString(TOKEN_KEY, null);
                }
            }
            
            if (token == null) {
                Log.d(TAG, "getToken: токен не найден");
            } else {
                Log.d(TAG, "getToken: токен найден, длина=" + token.length());
            }
            
            return token;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении токена", e);
            return null;
        }
    }

    // Метод для получения ID пользователя
    public String getUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(USER_ID_KEY, null);
    }

    // Метод для получения имени пользователя
    public String getUsername() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(USERNAME_KEY, null);
    }

    // Проверка, авторизован ли пользователь
    public boolean isAuthenticated() {
        String token = getToken();
        boolean hasToken = token != null && !token.isEmpty();
        Log.d(TAG, "Проверка аутентификации: token=" + (hasToken ? "имеется" : "отсутствует"));
        return hasToken;
    }

    // Выход (удаление данных авторизации)
    public void logout() {
        try {
            // Очищаем основной файл SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            boolean mainSuccess = editor.commit();
            
            // Очищаем резервный файл
            SharedPreferences backupPrefs = context.getSharedPreferences(PREF_NAME + "_backup", Context.MODE_PRIVATE);
            SharedPreferences.Editor backupEditor = backupPrefs.edit();
            backupEditor.clear();
            boolean backupSuccess = backupEditor.commit();
            
            // Удаляем файл с данными авторизации
            java.io.File file = new java.io.File(context.getFilesDir(), "auth_data.json");
            boolean fileDeleted = false;
            if (file.exists()) {
                fileDeleted = file.delete();
            }
            
            Log.d(TAG, "Выполнен выход (logout): основной=" + mainSuccess + 
                  ", резервный=" + backupSuccess + 
                  ", файл=" + (fileDeleted ? "удалён" : "не найден"));
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при выполнении logout", e);
        }
    }

    // Регистрация нового пользователя
    public ApiResponse<Void> register(String username, String email, String password, String gender, String phone) {
        Log.d(TAG, "Попытка регистрации пользователя: " + username + ", " + email);
        
        // Имитируем успешную регистрацию без сервера для отладки
        try {
            // Задержка для имитации сетевого запроса
            Thread.sleep(1000);
            
            // Генерируем фейковый идентификатор пользователя
            String userId = String.valueOf(System.currentTimeMillis() % 10000);
            
            // Сохраняем данные аутентификации
            saveAuthData("fake_token_" + userId, userId, username);
            
            Log.d(TAG, "Успешная регистрация пользователя: " + username + ", ID: " + userId);
            return new ApiResponse<>(true, "Регистрация успешна", null);
            
        } catch (InterruptedException e) {
            Log.e(TAG, "Ошибка при имитации регистрации", e);
            return new ApiResponse<>(false, "Ошибка регистрации: " + e.getMessage(), null);
        }
        
        /* Закомментируем реальный сетевой код
        RegisterRequest registerRequest = new RegisterRequest(username, email, password, gender, phone);
        
        RequestBody body = RequestBody.create(registerRequest.toJson(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/register")
                .post(body)
                .build();
        
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseString = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseString);
                
                String token = jsonResponse.getString("token");
                String userId = jsonResponse.getString("userId");
                String usernameResponse = jsonResponse.getString("username");
                
                saveAuthData(token, userId, usernameResponse);
                return new ApiResponse<>(true, "Регистрация успешна", null);
            } else {
                String errorBody = response.body().string();
                return new ApiResponse<>(false, getErrorMessage(errorBody), null);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Ошибка при регистрации", e);
            return new ApiResponse<>(false, "Ошибка соединения: " + e.getMessage(), null);
        }
        */
    }

    // Регистрация нового пользователя (асинхронная версия с колбэком)
    public void register(String username, String email, String password, String gender, String phone, ApiResponseCallback callback) {
        executorService.execute(() -> {
            ApiResponse<Void> response = register(username, email, password, gender, phone);
            if (response.isSuccess()) {
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(response.getMessage()));
            }
        });
    }

    // Вход пользователя (синхронная версия)
    public ApiResponse<Void> login(String email, String password) {
        Log.d(TAG, "Попытка входа пользователя: " + email);
        
        // Имитируем успешный вход без сервера для отладки
        try {
            // Задержка для имитации сетевого запроса
            Thread.sleep(1000);
            
            // Создадим простое имя пользователя из email (до символа @)
            String username = email.split("@")[0];
            
            // Генерируем фейковый идентификатор пользователя и токен
            String userId = String.valueOf(Math.abs(email.hashCode()) % 10000);
            String token = "fake_token_" + userId + "_" + System.currentTimeMillis();
            
            // Проверим текущее состояние SharedPreferences перед сохранением
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Log.d(TAG, "Состояние SharedPreferences перед сохранением: " + 
                    "token=" + prefs.getString(TOKEN_KEY, "нет") + 
                    ", userId=" + prefs.getString(USER_ID_KEY, "нет") + 
                    ", username=" + prefs.getString(USERNAME_KEY, "нет"));
            
            // Сохраняем данные аутентификации
            saveAuthData(token, userId, username);
            
            // Проверяем после сохранения
            String savedToken = getToken();
            Log.d(TAG, "После сохранения: token=" + (savedToken != null ? savedToken : "нет"));
            
            Log.d(TAG, "Успешный вход пользователя: " + username + ", ID: " + userId);
            return new ApiResponse<>(true, "Вход выполнен успешно", null);
            
        } catch (InterruptedException e) {
            Log.e(TAG, "Ошибка при имитации входа", e);
            return new ApiResponse<>(false, "Ошибка входа: " + e.getMessage(), null);
        }
        
        /* Закомментируем реальный сетевой код
        LoginRequest loginRequest = new LoginRequest(email, password);
        
        RequestBody body = RequestBody.create(loginRequest.toJson(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/login")
                .post(body)
                .build();
        
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseString = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseString);
                
                String token = jsonResponse.getString("token");
                String userId = jsonResponse.getString("userId");
                String username = jsonResponse.getString("username");
                
                saveAuthData(token, userId, username);
                return new ApiResponse<>(true, "Вход выполнен успешно", null);
            } else {
                String errorBody = response.body().string();
                return new ApiResponse<>(false, getErrorMessage(errorBody), null);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Ошибка при входе", e);
            return new ApiResponse<>(false, "Ошибка соединения: " + e.getMessage(), null);
        }
        */
    }
    
    // Вход пользователя (асинхронная версия с колбэком)
    public void login(String email, String password, ApiResponseCallback callback) {
        executorService.execute(() -> {
            ApiResponse<Void> response = login(email, password);
            if (response.isSuccess()) {
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(response.getMessage()));
            }
        });
    }

    // Получение данных пользователя
    public ApiResponse<UserResponse> getUserData() {
        String token = getToken();
        String userId = getUserId();
        String username = getUsername();
        
        if (token == null || userId == null) {
            return new ApiResponse<>(false, "Пользователь не авторизован", null);
        }
        
        Log.d(TAG, "Получение данных пользователя: ID " + userId);
        
        // Создаем тестовые данные пользователя
        try {
            Thread.sleep(500); // Имитация задержки сети
            
            // Получаем сохраненные данные пользователя из SharedPreferences
            SharedPreferences userPrefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE);
            String email = userPrefs.getString("email", username + "@gmail.com");
            String phone = userPrefs.getString("phone", "+7" + userId + "000000");
            String gender = userPrefs.getString("gender", "не указан");
            
            // Создаем объект User с данными из SharedPreferences
            UserResponse.User user = new UserResponse.User(
                userId, 
                username, 
                email, 
                gender, 
                phone, 
                "1", 
                "1", 
                "1"
            );
            
            // Получаем данные компании из SharedPreferences
            String companyName = context.getSharedPreferences("company_data", Context.MODE_PRIVATE)
                .getString("company_name", "Тестовая компания");
            
            // Создаем объект Company (или null, если компания не задана)
            UserResponse.Company company = new UserResponse.Company(
                companyName, 
                "Россия", 
                "ООО", 
                "Товары", 
                "Локальная", 
                "Москва"
            );
            
            // Создаем Map для ответов
            Map<String, String> answers = new HashMap<>();
            
            // Создаем UserResponse с корректными аргументами
            UserResponse userResponse = new UserResponse(user, company, answers);
            
            Log.d(TAG, "Данные пользователя получены для ID: " + userId + ", email: " + email);
            return new ApiResponse<>(true, "Данные пользователя получены", userResponse);
        } catch (InterruptedException e) {
            Log.e(TAG, "Ошибка при имитации получения данных пользователя", e);
            return new ApiResponse<>(false, "Ошибка: " + e.getMessage(), null);
        }
        
        /* Закомментируем реальный сетевой код
        Request request = new Request.Builder()
                .url(BASE_URL + "/user/" + userId)
                .header("Authorization", "Bearer " + token)
                .build();
        
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseString = response.body().string();
                UserResponse userResponse = UserResponse.fromJson(responseString);
                return new ApiResponse<>(true, "Данные пользователя получены", userResponse);
            } else {
                String errorBody = response.body().string();
                return new ApiResponse<>(false, getErrorMessage(errorBody), null);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Ошибка при получении данных пользователя", e);
            return new ApiResponse<>(false, "Ошибка соединения: " + e.getMessage(), null);
        }
        */
    }

    // Обновление данных компании
    public ApiResponse<Void> updateCompany(String companyName, String country, String type, 
                                           String products, String geography, String city) {
        String token = getToken();
        
        if (token == null) {
            return new ApiResponse<>(false, "Пользователь не авторизован", null);
        }
        
        Log.d(TAG, "Обновление данных компании: " + companyName);
        
        // Имитируем успешное обновление
        try {
            Thread.sleep(500); // Имитация задержки сети
            Log.d(TAG, "Данные компании успешно обновлены");
            return new ApiResponse<>(true, "Данные компании успешно обновлены", null);
        } catch (InterruptedException e) {
            Log.e(TAG, "Ошибка при имитации обновления компании", e);
            return new ApiResponse<>(false, "Ошибка: " + e.getMessage(), null);
        }
        
        /* Закомментируем реальный сетевой код
        CompanyRequest companyRequest = new CompanyRequest(
                companyName, country, type, products, geography, city);
        
        RequestBody body = RequestBody.create(companyRequest.toJson(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/company/update")
                .header("Authorization", "Bearer " + token)
                .post(body)
                .build();
        
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return new ApiResponse<>(true, "Данные компании успешно обновлены", null);
            } else {
                String errorBody = response.body().string();
                return new ApiResponse<>(false, getErrorMessage(errorBody), null);
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при обновлении данных компании", e);
            return new ApiResponse<>(false, "Ошибка соединения: " + e.getMessage(), null);
        }
        */
    }

    // Сохранение ответов на вопросы
    public ApiResponse<Void> saveAnswers(Map<String, String> answers) {
        String token = getToken();
        
        if (token == null) {
            return new ApiResponse<>(false, "Пользователь не авторизован", null);
        }
        
        Log.d(TAG, "Сохранение ответов: " + answers.size() + " ответов");
        
        // Имитируем успешное сохранение
        try {
            Thread.sleep(500); // Имитация задержки сети
            Log.d(TAG, "Ответы успешно сохранены");
            return new ApiResponse<>(true, "Ответы успешно сохранены", null);
        } catch (InterruptedException e) {
            Log.e(TAG, "Ошибка при имитации сохранения ответов", e);
            return new ApiResponse<>(false, "Ошибка: " + e.getMessage(), null);
        }
        
        /* Закомментируем реальный сетевой код
        AnswerRequest answerRequest = new AnswerRequest(answers);
        
        RequestBody body = RequestBody.create(answerRequest.toJson(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/answers/save")
                .header("Authorization", "Bearer " + token)
                .post(body)
                .build();
        
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return new ApiResponse<>(true, "Ответы успешно сохранены", null);
            } else {
                String errorBody = response.body().string();
                return new ApiResponse<>(false, getErrorMessage(errorBody), null);
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при сохранении ответов", e);
            return new ApiResponse<>(false, "Ошибка соединения: " + e.getMessage(), null);
        }
        */
    }

    // Получение вопросов
    public ApiResponse<String> getQuestions() {
        String token = getToken();
        
        if (token == null) {
            return new ApiResponse<>(false, "Пользователь не авторизован", null);
        }
        
        Log.d(TAG, "Запрос на получение вопросов");
        
        // Создаем тестовый набор вопросов
        try {
            Thread.sleep(500); // Имитация задержки сети
            
            // Создаем JSON с тестовыми вопросами
            JSONObject questionsJson = new JSONObject();
            JSONArray questionsArray = new JSONArray();
            
            // Тестовый вопрос 1
            JSONObject q1 = new JSONObject();
            q1.put("id", "1");
            q1.put("text", "Текущее состояние вашего бизнеса?");
            q1.put("type", "radio");
            q1.put("options", new JSONArray(new String[]{"В разработке", "Запущен", "В развитии"}));
            questionsArray.put(q1);
            
            // Тестовый вопрос 2
            JSONObject q2 = new JSONObject();
            q2.put("id", "2");
            q2.put("text", "Какие ваши основные цели, и для кого вы создаете продукт или услугу?");
            q2.put("type", "text");
            questionsArray.put(q2);
            
            questionsJson.put("questions", questionsArray);
            
            Log.d(TAG, "Получены тестовые вопросы: " + questionsArray.length());
            return new ApiResponse<>(true, "Вопросы получены", questionsJson.toString());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при создании тестовых вопросов", e);
            return new ApiResponse<>(false, "Ошибка: " + e.getMessage(), null);
        }
        
        /* Закомментируем реальный сетевой код
        Request request = new Request.Builder()
                .url(BASE_URL + "/questions")
                .header("Authorization", "Bearer " + token)
                    .build();
        
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseString = response.body().string();
                return new ApiResponse<>(true, "Вопросы получены", responseString);
            } else {
                String errorBody = response.body().string();
                return new ApiResponse<>(false, getErrorMessage(errorBody), null);
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при получении вопросов", e);
            return new ApiResponse<>(false, "Ошибка соединения: " + e.getMessage(), null);
        }
        */
    }

    // Получение сообщения об ошибке из тела ответа
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
    
    /**
     * Сохраняет данные базовой анкеты на сервере
     * 
     * @param questionnaire объект с данными анкеты
     * @param callback обратный вызов для получения результата
     */
    public void saveBasicQuestionnaire(BasicQuestionnaire questionnaire, ApiResponseCallback callback) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("userId", questionnaire.getUserId());
            jsonData.put("companyName", questionnaire.getCompanyName());
            jsonData.put("country", questionnaire.getCountry());
            jsonData.put("activityType", questionnaire.getActivityType());
            jsonData.put("productsServicesDescription", questionnaire.getProductsServicesDescription());
            jsonData.put("marketScope", questionnaire.getMarketScope());
            jsonData.put("businessState", questionnaire.getBusinessState());
            
            // Добавляем дополнительные поля в зависимости от типа географии реализации
            if ("Локальная".equals(questionnaire.getMarketScope())) {
                jsonData.put("city", questionnaire.getCity());
            } else if ("Национальная".equals(questionnaire.getMarketScope())) {
                jsonData.put("targetCountry", questionnaire.getTargetCountry());
            } else if ("Международная".equals(questionnaire.getMarketScope())) {
                JSONArray countriesArray = new JSONArray();
                for (String country : questionnaire.getTargetCountries()) {
                    countriesArray.put(country);
                }
                jsonData.put("targetCountries", countriesArray);
            }
            
            makeRequest("/questionnaire", RequestMethod.POST, jsonData, callback);
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError("Ошибка при формировании запроса: " + e.getMessage());
            }
        }
    }

    /**
     * Получает данные базовой анкеты с сервера
     * 
     * @param callback обратный вызов для получения результата
     */
    public void getBasicQuestionnaire(ApiResponseCallback callback) {
        makeRequest("/questionnaire", RequestMethod.GET, null, callback);
    }

    /**
     * Обновляет данные профиля пользователя
     * 
     * @param name имя пользователя
     * @param email электронная почта
     * @param phone телефон
     * @param role роль
     * @param companyName название компании
     * @param callback обратный вызов для получения результата
     */
    public void updateProfile(String name, String email, String phone, String gender, String role, String companyName, ApiResponseCallback callback) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("name", name);
            jsonData.put("email", email);
            jsonData.put("phone", phone);
            jsonData.put("gender", gender);
            jsonData.put("role", role);
            jsonData.put("companyName", companyName);
            
            makeRequest("/profile", RequestMethod.PUT, jsonData, callback);
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError("Ошибка при формировании запроса: " + e.getMessage());
            }
        }
    }

    // Перечисление типов HTTP-запросов
    private enum RequestMethod {
        GET, POST, PUT, DELETE
    }
    
    /**
     * Выполняет HTTP-запрос к серверу
     */
    private void makeRequest(String endpoint, RequestMethod method, JSONObject data, ApiResponseCallback callback) {
        // Временная имитация запроса к серверу
        executorService.execute(() -> {
            try {
                // Имитация сетевой задержки
                Thread.sleep(1000);
                
                // Имитация успешного ответа (вызов в главном потоке)
                mainHandler.post(() -> callback.onSuccess("Запрос успешно выполнен"));
            } catch (InterruptedException e) {
                // Обработка ошибки (вызов в главном потоке)
                mainHandler.post(() -> callback.onError("Ошибка выполнения запроса: " + e.getMessage()));
            }
        });
    }

    /**
     * Запуск локального сервера, если он еще не запущен
     * Метод отключен, так как вызывает ошибку NoClassDefFoundError (sun.misc.Service)
     * В Android SDK нет доступа к пакету com.sun.net.httpserver
     */
    private void startServerIfNeeded() {
        // Метод отключен
        Log.i(TAG, "Локальный сервер отключен. Используются только мокированные ответы API.");
        /*
        if (!isServerRunning) {
            executorService.execute(() -> {
                try {
                    ApiServer server = ApiServer.getInstance();
                    server.start();
                    isServerRunning = true;
                    Log.i(TAG, "Локальный сервер успешно запущен");
                } catch (IOException e) {
                    Log.e(TAG, "Ошибка при запуске локального сервера", e);
                }
            });
        }
        */
    }

    // Сохранение данных авторизации в файл
    private void saveAuthDataToFile(String token, String userId, String username) {
        try {
            // Создаем объект JSON с данными авторизации
            JSONObject authData = new JSONObject();
            authData.put(TOKEN_KEY, token);
            authData.put(USER_ID_KEY, userId);
            authData.put(USERNAME_KEY, username);
            
            // Конвертируем в строку
            String jsonData = authData.toString();
            
            // Сохраняем в файл в приватной директории приложения
            java.io.File file = new java.io.File(context.getFilesDir(), "auth_data.json");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(jsonData);
            writer.flush();
            writer.close();
            
            Log.d(TAG, "Данные авторизации сохранены в файл: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении данных в файл", e);
        }
    }

    // Попытка восстановления данных из файла
    private void tryRestoreAuthDataFromFile() {
        try {
            // Проверяем существование файла
            java.io.File file = new java.io.File(context.getFilesDir(), "auth_data.json");
            if (!file.exists()) {
                Log.d(TAG, "Файл с данными авторизации не найден");
                return;
            }
            
            // Читаем файл
            StringBuilder sb = new StringBuilder();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            // Парсим JSON
            JSONObject authData = new JSONObject(sb.toString());
            String token = authData.getString(TOKEN_KEY);
            String userId = authData.getString(USER_ID_KEY);
            String username = authData.getString(USERNAME_KEY);
            
            if (token != null && userId != null && username != null) {
                Log.d(TAG, "Восстановление данных авторизации из файла");
                saveAuthData(token, userId, username);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при восстановлении данных из файла", e);
        }
    }
}

                for (String country : questionnaire.getTargetCountries()) {
                    countriesArray.put(country);
                }
                jsonData.put("targetCountries", countriesArray);
            }
            
            makeRequest("/questionnaire", RequestMethod.POST, jsonData, callback);
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError("Ошибка при формировании запроса: " + e.getMessage());
            }
        }
    }

    /**
     * Получает данные базовой анкеты с сервера
     * 
     * @param callback обратный вызов для получения результата
     */
    public void getBasicQuestionnaire(ApiResponseCallback callback) {
        makeRequest("/questionnaire", RequestMethod.GET, null, callback);
    }

    /**
     * Обновляет данные профиля пользователя
     * 
     * @param name имя пользователя
     * @param email электронная почта
     * @param phone телефон
     * @param role роль
     * @param companyName название компании
     * @param callback обратный вызов для получения результата
     */
    public void updateProfile(String name, String email, String phone, String gender, String role, String companyName, ApiResponseCallback callback) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("name", name);
            jsonData.put("email", email);
            jsonData.put("phone", phone);
            jsonData.put("gender", gender);
            jsonData.put("role", role);
            jsonData.put("companyName", companyName);
            
            makeRequest("/profile", RequestMethod.PUT, jsonData, callback);
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError("Ошибка при формировании запроса: " + e.getMessage());
            }
        }
    }

    // Перечисление типов HTTP-запросов
    private enum RequestMethod {
        GET, POST, PUT, DELETE
    }
    
    /**
     * Выполняет HTTP-запрос к серверу
     */
    private void makeRequest(String endpoint, RequestMethod method, JSONObject data, ApiResponseCallback callback) {
        // Временная имитация запроса к серверу
        executorService.execute(() -> {
            try {
                // Имитация сетевой задержки
                Thread.sleep(1000);
                
                // Имитация успешного ответа (вызов в главном потоке)
                mainHandler.post(() -> callback.onSuccess("Запрос успешно выполнен"));
            } catch (InterruptedException e) {
                // Обработка ошибки (вызов в главном потоке)
                mainHandler.post(() -> callback.onError("Ошибка выполнения запроса: " + e.getMessage()));
            }
        });
    }

    /**
     * Запуск локального сервера, если он еще не запущен
     * Метод отключен, так как вызывает ошибку NoClassDefFoundError (sun.misc.Service)
     * В Android SDK нет доступа к пакету com.sun.net.httpserver
     */
    private void startServerIfNeeded() {
        // Метод отключен
        Log.i(TAG, "Локальный сервер отключен. Используются только мокированные ответы API.");
        /*
        if (!isServerRunning) {
            executorService.execute(() -> {
                try {
                    ApiServer server = ApiServer.getInstance();
                    server.start();
                    isServerRunning = true;
                    Log.i(TAG, "Локальный сервер успешно запущен");
                } catch (IOException e) {
                    Log.e(TAG, "Ошибка при запуске локального сервера", e);
                }
            });
        }
        */
    }

    // Сохранение данных авторизации в файл
    private void saveAuthDataToFile(String token, String userId, String username) {
        try {
            // Создаем объект JSON с данными авторизации
            JSONObject authData = new JSONObject();
            authData.put(TOKEN_KEY, token);
            authData.put(USER_ID_KEY, userId);
            authData.put(USERNAME_KEY, username);
            
            // Конвертируем в строку
            String jsonData = authData.toString();
            
            // Сохраняем в файл в приватной директории приложения
            java.io.File file = new java.io.File(context.getFilesDir(), "auth_data.json");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(jsonData);
            writer.flush();
            writer.close();
            
            Log.d(TAG, "Данные авторизации сохранены в файл: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении данных в файл", e);
        }
    }

    // Попытка восстановления данных из файла
    private void tryRestoreAuthDataFromFile() {
        try {
            // Проверяем существование файла
            java.io.File file = new java.io.File(context.getFilesDir(), "auth_data.json");
            if (!file.exists()) {
                Log.d(TAG, "Файл с данными авторизации не найден");
                return;
            }
            
            // Читаем файл
            StringBuilder sb = new StringBuilder();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            // Парсим JSON
            JSONObject authData = new JSONObject(sb.toString());
            String token = authData.getString(TOKEN_KEY);
            String userId = authData.getString(USER_ID_KEY);
            String username = authData.getString(USERNAME_KEY);
            
            if (token != null && userId != null && username != null) {
                Log.d(TAG, "Восстановление данных авторизации из файла");
                saveAuthData(token, userId, username);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при восстановлении данных из файла", e);
        }
    }
}
