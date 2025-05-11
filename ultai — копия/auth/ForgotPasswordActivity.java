package com.example.ultai.auth;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ultai.R;
import com.example.ultai.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        // Кнопка назад
        binding.backButton.setOnClickListener(v -> finish());

        // Кнопка отправки запроса на сброс пароля
        binding.resetPasswordButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = binding.emailEditText.getText().toString().trim();
        
        // Валидация email
        if (email.isEmpty()) {
            binding.emailEditText.setError("Введите email");
            return;
        }
        
        // Здесь будет логика для сброса пароля
        Toast.makeText(this, getString(R.string.forgot_password_success), 
                Toast.LENGTH_LONG).show();
                
        // Закрыть активность после успешной отправки
        finish();
    }
} 