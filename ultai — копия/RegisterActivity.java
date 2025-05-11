package com.example.ultai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ultai.api.ApiClient;
import com.example.ultai.auth.LoginActivity;
import com.example.ultai.ultai.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText genderEditText;
    private EditText phoneEditText;
    private Button registerButton;
    private TextView loginTextView;
    private ProgressBar progressBar;
    
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация компонентов UI
        usernameEditText = findViewById(R.id.username_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        genderEditText = findViewById(R.id.gender_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        registerButton = findViewById(R.id.register_button);
        loginTextView = findViewById(R.id.login_text_view);
        progressBar = findViewById(R.id.progress_bar);
        
        // Получение экземпляра ApiClient
        apiClient = ApiClient.getInstance(this);
        
        // Обработчик нажатия на кнопку регистрации
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        
        // Обработчик нажатия на текст входа
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Возвращаемся на экран входа
                finish();
            }
        });
    }
    
    private void register() {
        // Получение введенных данных
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String gender = genderEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        
        // Проверка корректности введенных данных
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Введите имя пользователя");
            return;
        }
        
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Введите email");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Введите пароль");
            return;
        }
        
        if (password.length() < 6) {
            passwordEditText.setError("Пароль должен содержать не менее 6 символов");
            return;
        }
        
        // Показываем прогресс
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
        
        // Выполняем регистрацию
        apiClient.register(username, email, password, gender, phone, new ApiClient.ApiResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                // Скрываем прогресс
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                
                // Переходим на главный экран
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                // Скрываем прогресс
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                
                // Показываем сообщение об ошибке
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
} 