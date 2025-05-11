package com.example.ultai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ultai.api.ApiClient;
import com.example.ultai.api.ApiServer;
import com.example.ultai.auth.LoginActivity;
import com.example.ultai.ultai.MainActivity;

import java.io.IOException;

public class MainActivityLauncher extends AppCompatActivity {

    private static final String TAG = "MainActivityLauncher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_launcher);

        // Инициализируем ApiClient
        ApiClient apiClient = ApiClient.getInstance(this);
        
        // Получаем данные авторизации для логирования
        String token = apiClient.getToken();
        String userId = apiClient.getUserId();
        String username = apiClient.getUsername();
        
        Log.d(TAG, "Проверка состояния авторизации: " +
              "token=" + (token != null ? "есть" : "нет") +
              ", userId=" + userId +
              ", username=" + username);
        
        // Перепроверяем, все ли данные на месте
        boolean isAuthenticated = token != null && !token.isEmpty() && 
                                 userId != null && !userId.isEmpty() &&
                                 username != null && !username.isEmpty();
        
        Log.d(TAG, "Состояние авторизации: " + isAuthenticated);

        // Запуск соответствующей активности после проверки аутентификации
        Intent intent;
        if (isAuthenticated) {
            // Если пользователь аутентифицирован, запускаем MainActivity с флагом isAuthenticated=true
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("isAuthenticated", true);
            Log.i(TAG, "Пользователь аутентифицирован, переход на главный экран");
        } else {
            // Если пользователь не аутентифицирован, направляем на начальный экран FirstFragment
            // через MainActivity, используя специальный флаг для открытия FirstFragment
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("isAuthenticated", false);
            intent.putExtra("openFirstFragment", true);
            Log.i(TAG, "Пользователь не аутентифицирован, переход на экран First Fragment");
        }

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Сервер теперь не запускается, поэтому не требуется его останавливать
        /*
        // Останавливаем сервер при закрытии приложения
        try {
            ApiServer.getInstance().stop();
            Log.i(TAG, "Сервер остановлен");
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при остановке сервера", e);
        }
        */
    }
}
