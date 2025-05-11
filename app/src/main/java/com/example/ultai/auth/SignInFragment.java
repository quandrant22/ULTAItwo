package com.example.ultai.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

import com.example.ultai20.R;
// Удаляем импорты ApiClient
// import com.example.ultai.api.ApiClient;
// import com.example.ultai.api.ApiResponse;
// import com.example.ultai.data.entities.UserEntity; // Больше не используется
import com.example.ultai.data.repository.UserRepository;
import com.example.ultai20.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth; // Добавляем для проверки состояния
import com.google.firebase.auth.FirebaseUser; // Добавляем для Callback

// Удаляем ExecutorService
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;

public class SignInFragment extends Fragment {

    private static final String TAG = "SignInFragment";
    private ActivityLoginBinding binding;
    // private ApiClient apiClient; // Удаляем
    private UserRepository userRepository;
    // private ExecutorService executorService; // Удаляем
    private FirebaseAuth firebaseAuth; // Добавляем для проверки

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // apiClient = ApiClient.getInstance(requireContext()); // Удаляем
        userRepository = UserRepository.getInstance(); // Получаем экземпляр без контекста
        // executorService = Executors.newSingleThreadExecutor(); // Удаляем
        firebaseAuth = FirebaseAuth.getInstance();

        // Проверка: Если пользователь УЖЕ авторизован в Firebase, перенаправляем
        if (firebaseAuth.getCurrentUser() != null) {
             Log.d(TAG, "User already signed in, navigating to home.");
             navigateToHome();
             return; // Важно выйти, чтобы остальной код onViewCreated не выполнялся
        }

        // Переход на экран регистрации
        binding.createAccountTextView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_signInFragment_to_registrationFragment);
        });

        // Кнопка назад
        binding.backButton.setOnClickListener(v -> {
             // Вместо requireActivity().onBackPressed(); используем NavController для безопасности
             NavController navController = Navigation.findNavController(v);
             navController.popBackStack();
        });

        // Вход в систему
        binding.loginButton.setOnClickListener(v -> signIn());

        // Кнопки социальных сетей (оставляем заглушки)
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
            binding.emailEditText.requestFocus();
            return;
        }
        // Добавить более строгую проверку email?
        if (password.isEmpty()) {
            binding.passwordEditText.setError("Введите пароль");
            binding.passwordEditText.requestFocus();
            return;
        }

        // Показываем индикатор загрузки
        showLoading(true);

        // Используем UserRepository для входа через Firebase
        userRepository.login(email, password, new UserRepository.Callback<FirebaseUser>() { // Тип колбэка изменен на FirebaseUser
            @Override
            public void onSuccess(FirebaseUser firebaseUser) { // Параметр теперь FirebaseUser
                 // Успешный вход
                 Log.d(TAG, "Login successful for user: " + firebaseUser.getUid());
                 if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    });
                }
            }

            @Override
            public void onError(String message) {
                // Ошибка входа
                Log.e(TAG, "Login failed: " + message);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        // Показываем сообщение об ошибке
                        Toast.makeText(requireContext(), "Ошибка входа: " + message, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    // Метод-заглушка для входа через соцсети
    private void signInWithSocial(String provider) {
        Toast.makeText(requireContext(), "Вход через " + provider + " пока не реализован", Toast.LENGTH_SHORT).show();
    }

    // Переход на главный экран
    private void navigateToHome() {
        if (getView() != null) {
             // Используем NavOptions для очистки стека до home и предотвращения возврата на экран входа
             NavOptions navOptions = new NavOptions.Builder()
                     .setPopUpTo(R.id.mobile_navigation, true) // Очищаем весь стек навигации
                     .build();
             Navigation.findNavController(requireView()).navigate(R.id.navigation_home, null, navOptions);
        } else {
             Log.e(TAG, "Cannot navigate to home, view is null");
        }
    }

    // Управление индикатором загрузки
    private void showLoading(boolean isLoading) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        binding.loginButton.setEnabled(!isLoading);
        binding.emailEditText.setEnabled(!isLoading);
        binding.passwordEditText.setEnabled(!isLoading);
        binding.createAccountTextView.setEnabled(!isLoading); // Блокируем и другие интерактивные элементы
        binding.googleSignInButton.setEnabled(!isLoading);
        binding.facebookSignInButton.setEnabled(!isLoading);
        binding.appleSignInButton.setEnabled(!isLoading);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // executorService.shutdown(); // Удаляем
        binding = null; // Важно для предотвращения утечек памяти
    }
}