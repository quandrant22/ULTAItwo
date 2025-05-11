package com.example.ultai.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ultai.RegisterActivity;
import com.example.ultai.databinding.ActivityLoginBinding;
import com.example.ultai.ultai.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        // Кнопка назад
        binding.backButton.setOnClickListener(v -> finish());

        // Кнопка входа
        binding.loginButton.setOnClickListener(v -> attemptLogin());
        
        // Ссылка "Создать аккаунт"
        binding.createAccountTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // Ссылка "Забыли пароль"
        binding.forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
        
        // Кнопки социальных сетей
        binding.googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        binding.facebookSignInButton.setOnClickListener(v -> signInWithFacebook());
        binding.appleSignInButton.setOnClickListener(v -> signInWithApple());
    }
    
    private void attemptLogin() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        
        // Валидация полей
        if (email.isEmpty()) {
            binding.emailEditText.setError("Введите email");
            return;
        }
        
        if (password.isEmpty()) {
            binding.passwordEditText.setError("Введите пароль");
            return;
        }
        
        // Здесь будет логика аутентификации
        // Для демонстрации просто переходим на главный экран
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void signInWithGoogle() {
        Toast.makeText(this, "Вход через Google будет доступен позже", Toast.LENGTH_SHORT).show();
    }
    
    private void signInWithFacebook() {
        Toast.makeText(this, "Вход через Facebook будет доступен позже", Toast.LENGTH_SHORT).show();
    }
    
    private void signInWithApple() {
        Toast.makeText(this, "Вход через Apple будет доступен позже", Toast.LENGTH_SHORT).show();
    }
} 