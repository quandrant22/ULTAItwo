package com.example.ultai.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.ultai.api.ApiClient;
import com.example.ultai.api.ApiResponse;
import com.example.ultai.data.LocalDatabase;
import com.example.ultai.data.dao.UserDao;
import com.example.ultai.data.entities.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с пользователями.
 * Обеспечивает взаимодействие с локальной базой данных и API сервера.
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    
    private final ApiClient apiClient;
    private final UserDao userDao;
    private final ExecutorService executorService;
    
    // Синглтон для UserRepository
    private static UserRepository instance;
    
    // Получение экземпляра репозитория
    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context);
        }
        return instance;
    }
    
    // Приватный конструктор для Singleton паттерна
    private UserRepository(Context context) {
        LocalDatabase database = LocalDatabase.getInstance(context);
        userDao = database.userDao();
        apiClient = ApiClient.getInstance(context);
        executorService = Executors.newFixedThreadPool(4);
    }
    
    /**
     * Регистрация нового пользователя с сохранением в локальную БД
     */
    public void register(String username, String email, String password, String gender, String phone, final Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                // Проверяем, существует ли уже пользователь с таким email
                UserEntity existingUser = userDao.getUserByEmail(email);
                if (existingUser != null) {
                    // Пользователь с таким email уже существует
                    callback.onError("Пользователь с таким email уже зарегистрирован");
                    return;
                }
                
                // Создаем нового пользователя
                UserEntity newUser = new UserEntity(username, email, password, gender, phone);
                
                // Сначала регистрируем в API (или используем заглушку)
                ApiResponse<Void> response = apiClient.register(username, email, password, gender, phone);
                
                if (response.isSuccess()) {
                    // Если API вернуло успех, сохраняем в БД
                    newUser.setToken(apiClient.getToken());
                    newUser.setSynced(true);
                    
                    // Сохраняем в базу данных
                    long userId = userDao.insertUser(newUser);
                    
                    Log.d(TAG, "Пользователь успешно зарегистрирован и сохранен в БД: " + username + ", ID: " + userId);
                    callback.onSuccess(null);
                } else {
                    // Если API вернуло ошибку, также возвращаем ошибку
                    callback.onError(response.getMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при регистрации пользователя", e);
                callback.onError("Произошла ошибка при регистрации: " + e.getMessage());
            }
        });
    }
    
    /**
     * Вход пользователя с проверкой в локальной БД
     */
    public void login(String email, String password, final Callback<UserEntity> callback) {
        executorService.execute(() -> {
            try {
                // Сначала проверяем в локальной БД
                UserEntity localUser = userDao.login(email, password);
                
                if (localUser != null) {
                    // Пользователь найден в локальной БД
                    // Также пытаемся выполнить вход через API
                    ApiResponse<Void> response = apiClient.login(email, password);
                    
                    if (response.isSuccess()) {
                        // Обновляем токен в локальной БД
                        localUser.setToken(apiClient.getToken());
                        localUser.setSynced(true);
                        userDao.updateUser(localUser);
                    }
                    
                    callback.onSuccess(localUser);
                } else {
                    // Пользователь не найден в локальной БД
                    // Пытаемся войти через API
                    ApiResponse<Void> response = apiClient.login(email, password);
                    
                    if (response.isSuccess()) {
                        // Создаем запись в локальной БД
                        String username = apiClient.getUsername();
                        UserEntity newUser = new UserEntity();
                        newUser.setEmail(email);
                        newUser.setPassword(password); // В реальном приложении хранить в зашифрованном виде
                        newUser.setUsername(username);
                        newUser.setToken(apiClient.getToken());
                        newUser.setSynced(true);
                        
                        long id = userDao.insertUser(newUser);
                        newUser.setId((int) id);
                        
                        callback.onSuccess(newUser);
                    } else {
                        callback.onError(response.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при входе пользователя", e);
                callback.onError("Произошла ошибка при входе: " + e.getMessage());
            }
        });
    }
    
    /**
     * Получение пользователя по email
     */
    public void getUserByEmail(String email, final Callback<UserEntity> callback) {
        executorService.execute(() -> {
            try {
                UserEntity user = userDao.getUserByEmail(email);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError("Пользователь не найден");
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при получении пользователя", e);
                callback.onError(e.getMessage());
            }
        });
    }
    
    /**
     * Интерфейс для обратных вызовов операций репозитория
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }
} 
 

import android.content.Context;
import android.util.Log;

import com.example.ultai.api.ApiClient;
import com.example.ultai.api.ApiResponse;
import com.example.ultai.data.LocalDatabase;
import com.example.ultai.data.dao.UserDao;
import com.example.ultai.data.entities.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с пользователями.
 * Обеспечивает взаимодействие с локальной базой данных и API сервера.
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    
    private final ApiClient apiClient;
    private final UserDao userDao;
    private final ExecutorService executorService;
    
    // Синглтон для UserRepository
    private static UserRepository instance;
    
    // Получение экземпляра репозитория
    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context);
        }
        return instance;
    }
    
    // Приватный конструктор для Singleton паттерна
    private UserRepository(Context context) {
        LocalDatabase database = LocalDatabase.getInstance(context);
        userDao = database.userDao();
        apiClient = ApiClient.getInstance(context);
        executorService = Executors.newFixedThreadPool(4);
    }
    
    /**
     * Регистрация нового пользователя с сохранением в локальную БД
     */
    public void register(String username, String email, String password, String gender, String phone, final Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                // Проверяем, существует ли уже пользователь с таким email
                UserEntity existingUser = userDao.getUserByEmail(email);
                if (existingUser != null) {
                    // Пользователь с таким email уже существует
                    callback.onError("Пользователь с таким email уже зарегистрирован");
                    return;
                }
                
                // Создаем нового пользователя
                UserEntity newUser = new UserEntity(username, email, password, gender, phone);
                
                // Сначала регистрируем в API (или используем заглушку)
                ApiResponse<Void> response = apiClient.register(username, email, password, gender, phone);
                
                if (response.isSuccess()) {
                    // Если API вернуло успех, сохраняем в БД
                    newUser.setToken(apiClient.getToken());
                    newUser.setSynced(true);
                    
                    // Сохраняем в базу данных
                    long userId = userDao.insertUser(newUser);
                    
                    Log.d(TAG, "Пользователь успешно зарегистрирован и сохранен в БД: " + username + ", ID: " + userId);
                    callback.onSuccess(null);
                } else {
                    // Если API вернуло ошибку, также возвращаем ошибку
                    callback.onError(response.getMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при регистрации пользователя", e);
                callback.onError("Произошла ошибка при регистрации: " + e.getMessage());
            }
        });
    }
    
    /**
     * Вход пользователя с проверкой в локальной БД
     */
    public void login(String email, String password, final Callback<UserEntity> callback) {
        executorService.execute(() -> {
            try {
                // Сначала проверяем в локальной БД
                UserEntity localUser = userDao.login(email, password);
                
                if (localUser != null) {
                    // Пользователь найден в локальной БД
                    // Также пытаемся выполнить вход через API
                    ApiResponse<Void> response = apiClient.login(email, password);
                    
                    if (response.isSuccess()) {
                        // Обновляем токен в локальной БД
                        localUser.setToken(apiClient.getToken());
                        localUser.setSynced(true);
                        userDao.updateUser(localUser);
                    }
                    
                    callback.onSuccess(localUser);
                } else {
                    // Пользователь не найден в локальной БД
                    // Пытаемся войти через API
                    ApiResponse<Void> response = apiClient.login(email, password);
                    
                    if (response.isSuccess()) {
                        // Создаем запись в локальной БД
                        String username = apiClient.getUsername();
                        UserEntity newUser = new UserEntity();
                        newUser.setEmail(email);
                        newUser.setPassword(password); // В реальном приложении хранить в зашифрованном виде
                        newUser.setUsername(username);
                        newUser.setToken(apiClient.getToken());
                        newUser.setSynced(true);
                        
                        long id = userDao.insertUser(newUser);
                        newUser.setId((int) id);
                        
                        callback.onSuccess(newUser);
                    } else {
                        callback.onError(response.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при входе пользователя", e);
                callback.onError("Произошла ошибка при входе: " + e.getMessage());
            }
        });
    }
    
    /**
     * Получение пользователя по email
     */
    public void getUserByEmail(String email, final Callback<UserEntity> callback) {
        executorService.execute(() -> {
            try {
                UserEntity user = userDao.getUserByEmail(email);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError("Пользователь не найден");
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при получении пользователя", e);
                callback.onError(e.getMessage());
            }
        });
    }
    
    /**
     * Интерфейс для обратных вызовов операций репозитория
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }
} 
 