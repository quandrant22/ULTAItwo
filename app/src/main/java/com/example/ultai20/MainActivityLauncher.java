package com.example.ultai20;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivityLauncher extends AppCompatActivity {

    private static final String TAG = "MainActivityLauncher";
    private FirebaseAuth mAuth;
    private boolean activityStarted = false; // Флаг для предотвращения повторного запуска

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Установка темы Splash Screen (лучше делать через AndroidManifest.xml)
        Log.d(TAG, "onCreate started.");

        mAuth = FirebaseAuth.getInstance();

        // Немедленная проверка при создании
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (!activityStarted) {
             if (currentUser != null) {
                 // Проверка, не анонимный ли это пользователь (если вы их используете)
                 // if (currentUser.isAnonymous()) { ... }
                 Log.d(TAG, "User is signed in (immediate check): " + currentUser.getUid());
                 startMainActivity(true);
             } else {
                 Log.d(TAG, "User is signed out (immediate check).");
                 startMainActivity(false);
             }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listener не добавляется
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Listener не удаляется
    }

    private void startMainActivity(boolean isAuthenticated) {
        // Запускаем MainActivity только один раз
        if (!activityStarted) {
            activityStarted = true; // Устанавливаем флаг ПЕРЕД запуском
            Log.d(TAG, "Starting MainActivity with isAuthenticated = " + isAuthenticated);
            Intent intent = new Intent(MainActivityLauncher.this, MainActivity.class);
            intent.putExtra("isAuthenticated", isAuthenticated);
            // Флаги, чтобы MainActivity стала новой вершиной стека и предыдущие (Launcher) удалились
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Закрываем Launcher Activity немедленно
        } else {
             Log.d(TAG, "MainActivity already started, skipping.");
        }
    }
} 