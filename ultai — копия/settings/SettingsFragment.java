package com.example.ultai.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;
import com.example.ultai.api.ApiClient;
import com.example.ultai.databinding.FragmentSettingsBinding;



public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private ApiClient apiClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Создаем экземпляр SettingsViewModel
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Инициализируем ApiClient
        apiClient = ApiClient.getInstance(requireContext());
        
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton imageButtonNext = view.findViewById(R.id.imageButton2);
        imageButtonNext.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_settingsFragment_to_profileFragment);
        });
        
        // Добавляем обработчик для контейнера выхода
        View logoutContainer = view.findViewById(R.id.logoutContainer);
        logoutContainer.setOnClickListener(v -> {
            // Выход из аккаунта
            apiClient.logout();
            
            // Показываем уведомление
            Toast.makeText(requireContext(), "Выход выполнен", Toast.LENGTH_SHORT).show();
            
            // Переходим на начальный экран
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_settingsFragment_to_firstFragment);
        });
        
        // Создаем callback для обработки нажатия кнопки "Назад"
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Возвращаемся на предыдущий фрагмент
                Navigation.findNavController(view).popBackStack();
            }
        };

        // Регистрируем callback
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

}
