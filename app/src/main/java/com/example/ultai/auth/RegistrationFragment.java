package com.example.ultai.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;
import com.example.ultai.data.repository.UserRepository;
import com.example.ultai20.databinding.ActivityRegisterBinding;
import com.example.ultai.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationFragment extends Fragment {

    private ActivityRegisterBinding binding;
    private UserRepository userRepository;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userRepository = UserRepository.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        binding.registerButton.setOnClickListener(v -> registerUser());

        // Обработчик нажатия на кнопку назад
        binding.backButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Обработчик нажатия на TextView для перехода в SignInFragment
        binding.loginTextView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_registrationFragment_to_signInFragment);
        });
        
        // Закомментировал или удалил проверки на элементы социальных сетей,
        // так как их нет в текущем макете
        /*
        if (binding.googleSignUpButton != null) {
            binding.googleSignUpButton.setOnClickListener(v -> signUpWithSocial("Google"));
        }
        if (binding.facebookSignUpButton != null) {
            binding.facebookSignUpButton.setOnClickListener(v -> signUpWithSocial("Facebook"));
        }
        if (binding.appleSignUpButton != null) {
            binding.appleSignUpButton.setOnClickListener(v -> signUpWithSocial("Apple"));
        }
        */
    }

    private void signUpWithSocial(String provider) {
        Toast.makeText(requireContext(), "Регистрация через " + provider + " будет доступна позже", Toast.LENGTH_SHORT).show();
    }

    private void registerUser() {
        // Показать прогресс
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        binding.registerButton.setEnabled(false);
        
        // Получаем данные из полей
        String username = binding.usernameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString();
        
        // Проверка на пустые поля
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showError("Все поля должны быть заполнены");
            return;
        }
        
        // Проверка соответствия паролей
        if (!password.equals(confirmPassword)) {
            showError("Пароли не совпадают");
            return;
        }
        
        // Получение выбранного пола
        String gender = "не указан";
        
        // Используем EditText для пола вместо RadioGroup, который отсутствует в макете
        if (binding.genderEditText != null && !binding.genderEditText.getText().toString().trim().isEmpty()) {
            gender = binding.genderEditText.getText().toString().trim();
        }
        
        // Временная переменная для хранения пола
        final String selectedGender = gender;

        // Добавляем логирование для отладки
        Log.d("RegistrationDebug", "Отправка запроса на регистрацию: " 
              + "username=" + username 
              + ", email=" + email 
              + ", password=***"  
              + ", gender=" + selectedGender 
              + ", phone=" + phone);
        
        // Используем UserRepository для регистрации через Firebase
        userRepository.register(username, email, password, selectedGender, phone, new UserRepository.Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        binding.registerButton.setEnabled(true);
                        
                        // Регистрация выполнена успешно
                        Toast.makeText(requireContext(), "Регистрация выполнена успешно", Toast.LENGTH_SHORT).show();
                        // Переход на экран анкеты после успешной регистрации
                        NavController navController = Navigation.findNavController(getView());
                        navController.navigate(R.id.action_registrationFragment_to_basicQuestionnaireFragment);
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError(message);
                    });
                }
            }
        });
    }
    
    private void showError(String message) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.GONE);
        }
        binding.registerButton.setEnabled(true);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 