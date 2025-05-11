package com.example.ultai.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;
import com.example.ultai.api.ApiClient;
import com.example.ultai.api.ApiResponse;
import com.example.ultai.data.entities.UserEntity;
import com.example.ultai.data.repository.UserRepository;
import com.example.ultai.databinding.ActivityLoginBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignInFragment extends Fragment {

    private ActivityLoginBinding binding;
    private ApiClient apiClient;
    private UserRepository userRepository;
    private ExecutorService executorService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        apiClient = ApiClient.getInstance(requireContext());
        userRepository = UserRepository.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
        
        // Если пользователь уже авторизован, перейти на главную страницу
        if (apiClient.isAuthenticated()) {
            navigateToHome();
            return;
        }

        // Переход на экран регистрации
        binding.createAccountTextView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_signInFragment_to_registrationFragment);
        });

        // Кнопка назад
        binding.backButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Вход в систему
        binding.loginButton.setOnClickListener(v -> signIn());
        
        // Кнопки социальных сетей
        binding.googleSignInButton.setOnClickListener(v -> signInWithSocial("Google"));
        binding.facebookSignInButton.setOnClickListener(v -> signInWithSocial("Facebook"));
        binding.appleSignInButton.setOnClickListener(v -> signInWithSocial("Apple"));
    }
    
    private void signIn() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        
        // Проверка ввода
        if (email.isEmpty()) {
            binding.emailEditText.setError("Введите email");
            return;
        }
        if (password.isEmpty()) {
            binding.passwordEditText.setError("Введите пароль");
            return;
        }
        
        // Показываем индикатор загрузки (если есть в макете)
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        binding.loginButton.setEnabled(false);
        
        // Используем UserRepository для входа с проверкой в локальной БД
        userRepository.login(email, password, new UserRepository.Callback<UserEntity>() {
            @Override
            public void onSuccess(UserEntity result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        binding.loginButton.setEnabled(true);
                        
                        // Вход выполнен успешно
                        Toast.makeText(requireContext(), "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        binding.loginButton.setEnabled(true);
                        
                        // Ошибка входа
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
    
    private void signInWithSocial(String provider) {
        Toast.makeText(requireContext(), "Вход через " + provider + " будет доступен позже", Toast.LENGTH_SHORT).show();
    }
    
    private void navigateToHome() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_signInFragment_to_navigation_home);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
        binding = null;
    }
} 
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        binding.loginButton.setEnabled(true);
                        
                        // Ошибка входа
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
    
    private void signInWithSocial(String provider) {
        Toast.makeText(requireContext(), "Вход через " + provider + " будет доступен позже", Toast.LENGTH_SHORT).show();
    }
    
    private void navigateToHome() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_signInFragment_to_navigation_home);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
        binding = null;
    }
} 