package com.example.ultai.api;

import android.util.Log;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Локальный сервер для обработки API запросов в приложении.
 * Сервер запускается на порту 3000 и обрабатывает запросы к различным эндпоинтам.
 */
public class ApiServer {
    private static final String TAG = "ApiServer";
    private static final int PORT = 3000;
    private static ApiServer instance;
    private final HttpServer server;
    private final ExecutorService executorService;
    
    // Хранилище данных пользователей (временное, в реальном приложении использовать базу данных)
    private final Map<String, JSONObject> users = new HashMap<>();
    private final Map<String, JSONObject> profiles = new HashMap<>();
    private final Map<String, JSONObject> questionnaires = new HashMap<>();

    private ApiServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Регистрация обработчиков для различных эндпоинтов
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/user", new UserDataHandler());
        server.createContext("/api/profile", new ProfileHandler());
        server.createContext("/api/questionnaire", new QuestionnaireHandler());
        
        executorService = Executors.newFixedThreadPool(10);
        server.setExecutor(executorService);
    }

    /**
     * Получение экземпляра сервера (Singleton)
     */
    public static synchronized ApiServer getInstance() throws IOException {
        if (instance == null) {
            instance = new ApiServer();
        }
        return instance;
    }

    /**
     * Запуск сервера
     */
    public void start() {
        server.start();
        Log.i(TAG, "Сервер запущен на порту " + PORT);
    }

    /**
     * Остановка сервера
     */
    public void stop() {
        server.stop(0);
        executorService.shutdown();
        Log.i(TAG, "Сервер остановлен");
    }

    /**
     * Обработчик для регистрации пользователей
     */
    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    JSONObject requestData = parseRequestBody(exchange);
                    
                    // Получение данных регистрации
                    String username = requestData.getString("username");
                    String email = requestData.getString("email");
                    String password = requestData.getString("password");
                    String gender = requestData.getString("gender");
                    String phone = requestData.getString("phone");
                    
                    // Генерация ID пользователя
                    String userId = String.valueOf(System.currentTimeMillis() % 10000);
                    
                    // Создание объекта пользователя
                    JSONObject user = new JSONObject();
                    user.put("id", userId);
                    user.put("username", username);
                    user.put("email", email);
                    user.put("password", password); // В реальном приложении хешировать пароль
                    user.put("gender", gender);
                    user.put("phone", phone);
                    
                    // Сохранение пользователя
                    users.put(userId, user);
                    
                    // Создание токена
                    String token = "token_" + userId;
                    
                    // Создание ответа
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("message", "Регистрация успешна");
                    response.put("token", token);
                    response.put("userId", userId);
                    response.put("username", username);
                    
                    sendResponse(exchange, 200, response.toString());
                } else {
                    sendResponse(exchange, 405, "Метод не поддерживается");
                }
            } catch (JSONException e) {
                sendResponse(exchange, 400, "Некорректный JSON: " + e.getMessage());
            } catch (Exception e) {
                sendResponse(exchange, 500, "Внутренняя ошибка сервера: " + e.getMessage());
            }
        }
    }

    /**
     * Обработчик для аутентификации пользователей
     */
    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    JSONObject requestData = parseRequestBody(exchange);
                    
                    // Получение данных входа
                    String email = requestData.getString("email");
                    String password = requestData.getString("password");
                    
                    // Поиск пользователя по email
                    String userId = null;
                    String username = null;
                    
                    for (Map.Entry<String, JSONObject> entry : users.entrySet()) {
                        JSONObject user = entry.getValue();
                        if (email.equals(user.getString("email")) && 
                            password.equals(user.getString("password"))) {
                            userId = entry.getKey();
                            username = user.getString("username");
                            break;
                        }
                    }
                    
                    // Если пользователь не найден или пароль неверный
                    if (userId == null) {
                        // Для упрощения создадим пользователя
                        userId = String.valueOf(System.currentTimeMillis() % 10000);
                        username = email.split("@")[0];
                        
                        // Создание объекта пользователя
                        JSONObject user = new JSONObject();
                        user.put("id", userId);
                        user.put("username", username);
                        user.put("email", email);
                        user.put("password", password);
                        user.put("gender", "не указан");
                        user.put("phone", "");
                        
                        // Сохранение пользователя
                        users.put(userId, user);
                    }
                    
                    // Создание токена
                    String token = "token_" + userId;
                    
                    // Создание ответа
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("message", "Вход выполнен успешно");
                    response.put("token", token);
                    response.put("userId", userId);
                    response.put("username", username);
                    
                    sendResponse(exchange, 200, response.toString());
                } else {
                    sendResponse(exchange, 405, "Метод не поддерживается");
                }
            } catch (JSONException e) {
                sendResponse(exchange, 400, "Некорректный JSON: " + e.getMessage());
            } catch (Exception e) {
                sendResponse(exchange, 500, "Внутренняя ошибка сервера: " + e.getMessage());
            }
        }
    }

    /**
     * Обработчик для данных пользователя
     */
    private class UserDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Проверка аутентификации
                Headers headers = exchange.getRequestHeaders();
                String authToken = headers.getFirst("Authorization");
                
                if (authToken == null || !authToken.startsWith("Bearer ")) {
                    sendResponse(exchange, 401, "Отсутствует токен авторизации");
                    return;
                }
                
                // Получение токена
                String token = authToken.substring(7);
                String userId = token.replace("token_", "");
                
                // Проверка существования пользователя
                JSONObject user = users.get(userId);
                if (user == null) {
                    sendResponse(exchange, 404, "Пользователь не найден");
                    return;
                }
                
                if ("GET".equals(exchange.getRequestMethod())) {
                    // Создание ответа с данными пользователя
                    JSONObject userData = new JSONObject();
                    userData.put("id", user.getString("id"));
                    userData.put("username", user.getString("username"));
                    userData.put("email", user.getString("email"));
                    userData.put("gender", user.getString("gender"));
                    userData.put("phone", user.getString("phone"));
                    
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("user", userData);
                    
                    sendResponse(exchange, 200, response.toString());
                } else if ("PUT".equals(exchange.getRequestMethod())) {
                    // Обновление данных пользователя
                    JSONObject requestData = parseRequestBody(exchange);
                    
                    if (requestData.has("username")) {
                        user.put("username", requestData.getString("username"));
                    }
                    
                    if (requestData.has("email")) {
                        user.put("email", requestData.getString("email"));
                    }
                    
                    if (requestData.has("gender")) {
                        user.put("gender", requestData.getString("gender"));
                    }
                    
                    if (requestData.has("phone")) {
                        user.put("phone", requestData.getString("phone"));
                    }
                    
                    // Сохранение обновленных данных
                    users.put(userId, user);
                    
                    // Создание ответа
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("message", "Данные пользователя обновлены");
                    
                    sendResponse(exchange, 200, response.toString());
                } else {
                    sendResponse(exchange, 405, "Метод не поддерживается");
                }
            } catch (JSONException e) {
                sendResponse(exchange, 400, "Некорректный JSON: " + e.getMessage());
            } catch (Exception e) {
                sendResponse(exchange, 500, "Внутренняя ошибка сервера: " + e.getMessage());
            }
        }
    }

    /**
     * Обработчик для данных профиля пользователя
     */
    private class ProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Проверка аутентификации
                Headers headers = exchange.getRequestHeaders();
                String authToken = headers.getFirst("Authorization");
                
                if (authToken == null || !authToken.startsWith("Bearer ")) {
                    sendResponse(exchange, 401, "Отсутствует токен авторизации");
                    return;
                }
                
                // Получение токена
                String token = authToken.substring(7);
                String userId = token.replace("token_", "");
                
                // Проверка существования пользователя
                JSONObject user = users.get(userId);
                if (user == null) {
                    sendResponse(exchange, 404, "Пользователь не найден");
                    return;
                }
                
                if ("GET".equals(exchange.getRequestMethod())) {
                    // Получение профиля пользователя
                    JSONObject profile = profiles.get(userId);
                    
                    if (profile == null) {
                        // Создаем профиль с данными пользователя
                        profile = new JSONObject();
                        profile.put("name", user.getString("username"));
                        profile.put("email", user.getString("email"));
                        profile.put("phone", user.getString("phone"));
                        profile.put("gender", user.getString("gender"));
                        profile.put("role", "Пользователь");
                        profile.put("companyName", "Не указано");
                        
                        // Сохраняем профиль
                        profiles.put(userId, profile);
                    }
                    
                    // Создание ответа
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("profile", profile);
                    
                    sendResponse(exchange, 200, response.toString());
                } else if ("PUT".equals(exchange.getRequestMethod())) {
                    // Обновление профиля пользователя
                    JSONObject requestData = parseRequestBody(exchange);
                    
                    // Получаем текущий профиль или создаем новый
                    JSONObject profile = profiles.get(userId);
                    if (profile == null) {
                        profile = new JSONObject();
                    }
                    
                    // Обновляем поля
                    if (requestData.has("name")) {
                        profile.put("name", requestData.getString("name"));
                    }
                    
                    if (requestData.has("email")) {
                        profile.put("email", requestData.getString("email"));
                    }
                    
                    if (requestData.has("phone")) {
                        profile.put("phone", requestData.getString("phone"));
                    }
                    
                    if (requestData.has("gender")) {
                        profile.put("gender", requestData.getString("gender"));
                    }
                    
                    if (requestData.has("role")) {
                        profile.put("role", requestData.getString("role"));
                    }
                    
                    if (requestData.has("companyName")) {
                        profile.put("companyName", requestData.getString("companyName"));
                    }
                    
                    // Сохраняем обновленный профиль
                    profiles.put(userId, profile);
                    
                    // Создание ответа
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("message", "Профиль обновлен");
                    
                    sendResponse(exchange, 200, response.toString());
                } else {
                    sendResponse(exchange, 405, "Метод не поддерживается");
                }
            } catch (JSONException e) {
                sendResponse(exchange, 400, "Некорректный JSON: " + e.getMessage());
            } catch (Exception e) {
                sendResponse(exchange, 500, "Внутренняя ошибка сервера: " + e.getMessage());
            }
        }
    }

    /**
     * Обработчик для данных анкеты пользователя
     */
    private class QuestionnaireHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Проверка аутентификации
                Headers headers = exchange.getRequestHeaders();
                String authToken = headers.getFirst("Authorization");
                
                if (authToken == null || !authToken.startsWith("Bearer ")) {
                    sendResponse(exchange, 401, "Отсутствует токен авторизации");
                    return;
                }
                
                // Получение токена
                String token = authToken.substring(7);
                String userId = token.replace("token_", "");
                
                // Проверка существования пользователя
                JSONObject user = users.get(userId);
                if (user == null) {
                    sendResponse(exchange, 404, "Пользователь не найден");
                    return;
                }
                
                if ("GET".equals(exchange.getRequestMethod())) {
                    // Получение анкеты пользователя
                    JSONObject questionnaire = questionnaires.get(userId);
                    
                    if (questionnaire == null) {
                        // Возвращаем пустую анкету
                        questionnaire = new JSONObject();
                    }
                    
                    // Создание ответа
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("questionnaire", questionnaire);
                    
                    sendResponse(exchange, 200, response.toString());
                } else if ("POST".equals(exchange.getRequestMethod()) || "PUT".equals(exchange.getRequestMethod())) {
                    // Сохранение или обновление анкеты
                    JSONObject requestData = parseRequestBody(exchange);
                    
                    // Сохраняем анкету
                    questionnaires.put(userId, requestData);
                    
                    // Создание ответа
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("message", "Анкета сохранена");
                    
                    sendResponse(exchange, 200, response.toString());
                } else {
                    sendResponse(exchange, 405, "Метод не поддерживается");
                }
            } catch (JSONException e) {
                sendResponse(exchange, 400, "Некорректный JSON: " + e.getMessage());
            } catch (Exception e) {
                sendResponse(exchange, 500, "Внутренняя ошибка сервера: " + e.getMessage());
            }
        }
    }

    /**
     * Разбор тела запроса в формате JSON
     */
    private JSONObject parseRequestBody(HttpExchange exchange) throws IOException, JSONException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String requestBody = br.lines().collect(Collectors.joining());
            return new JSONObject(requestBody);
        }
    }

    /**
     * Отправка ответа клиенту
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
} 