package com.example.ultai.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;
import com.example.ultai.databinding.FragmentProfileBinding;
import com.google.android.material.snackbar.Snackbar;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private static boolean isSaving = false; // Флаг для отслеживания процесса сохранения

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Инициализация ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Использование ViewBinding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация UI и настройка слушателей
        setupUI();
        setupObservers();
        setupListeners();
    }
    
    private void setupUI() {
        // Настройка заголовка
        final TextView textView = binding.textProfile;
        viewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        
        // Настройка кнопки "Назад"
        ImageButton backButton = binding.imageButton;
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_navigation_home);
        });

        // Настройка кнопки "Настройки"
        ImageButton settingsButton = binding.imageView19;
        settingsButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_settingsFragment);
        });
        
        // Настройка кнопки редактирования
        ImageView editButton = binding.imageView20;
        editButton.setOnClickListener(v -> viewModel.toggleEditMode());
    }
    
    private void setupObservers() {
        // Наблюдение за данными профиля
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                // Обновляем UI с данными пользователя
                binding.textView29.setText(profile.getName());
                binding.editTextText2.setText(profile.getName());
                binding.editTextPhone2.setText(profile.getPhone());
                binding.editTextTextEmailAddress.setText(profile.getEmail());
            }
        });
        
        // Наблюдение за режимом редактирования
        viewModel.getIsEditMode().observe(getViewLifecycleOwner(), isEditMode -> {
            // Включаем/выключаем редактирование полей
            binding.editTextText2.setEnabled(isEditMode);
            binding.editTextPhone2.setEnabled(isEditMode);
            binding.editTextTextEmailAddress.setEnabled(isEditMode);
            
            // Изменяем внешний вид полей в зависимости от режима
            int backgroundResId = isEditMode ? 
                    R.drawable.rounded_rectangle_editable : 
                    R.drawable.rounded_rectangle3;
                    
            binding.editTextText2.setBackgroundResource(backgroundResId);
            binding.editTextPhone2.setBackgroundResource(backgroundResId);
            binding.editTextTextEmailAddress.setBackgroundResource(backgroundResId);
            
            // Меняем иконку кнопки редактирования
            binding.imageView20.setImageResource(isEditMode ? 
                    R.drawable.icon_save : 
                    R.drawable.redactirovanie);
            
            // Если выключаем режим редактирования - сохраняем данные
            if (!isEditMode) {
                saveProfileData();
            }
        });
        
        // Наблюдение за состоянием загрузки
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Можно добавить ProgressBar и управлять его видимостью
            // binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Наблюдение за сообщениями об ошибках
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupListeners() {
        // Обработка кнопки "Назад"
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Если в режиме редактирования - выходим из него
                if (viewModel.getIsEditMode().getValue() != null && 
                        viewModel.getIsEditMode().getValue()) {
                    viewModel.toggleEditMode();
                } else {
                    // Иначе возвращаемся на предыдущий фрагмент
                    Navigation.findNavController(requireView()).popBackStack();
                }
            }
        };

        // Регистрируем callback для обработки кнопки "Назад"
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }
    
    private void saveProfileData() {
        // Предотвращаем множественные сохранения
        if (isSaving) {
            return;
        }
        
        // Получаем текущий профиль
        ProfileViewModel.UserProfile currentProfile = viewModel.getUserProfile().getValue();
        if (currentProfile == null) {
            return;
        }
        
        // Проверяем, были ли изменены данные
        String newName = binding.editTextText2.getText().toString();
        String newEmail = binding.editTextTextEmailAddress.getText().toString();
        String newPhone = binding.editTextPhone2.getText().toString();
        
        boolean hasChanges = !newName.equals(currentProfile.getName()) ||
                            !newEmail.equals(currentProfile.getEmail()) ||
                            !newPhone.equals(currentProfile.getPhone());
        
        // Если данные не изменились, не вызываем сохранение
        if (!hasChanges) {
            return;
        }
        
        // Устанавливаем флаг сохранения
        isSaving = true;
        
        // Создаем новый профиль с обновленными данными
        ProfileViewModel.UserProfile updatedProfile = new ProfileViewModel.UserProfile(
                newName,
                newEmail,
                newPhone,
                currentProfile.getGender()
        );
        
        // Копируем остальные поля
        updatedProfile.setRole(currentProfile.getRole());
        updatedProfile.setCompanyName(currentProfile.getCompanyName());
        
        // Сохраняем данные
        viewModel.saveUserProfile(updatedProfile);
        
        // Обновляем имя в заголовке профиля
        binding.textView29.setText(updatedProfile.getName());
        
        // Показываем сообщение пользователю через Snackbar вместо Toast
        Snackbar snackbar = Snackbar.make(binding.getRoot(), 
                getString(R.string.profile_save_success), 
                Snackbar.LENGTH_SHORT);
        
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                // Сбрасываем флаг сохранения после закрытия уведомления
                isSaving = false;
            }
        });
        
        snackbar.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Очищаем binding при уничтожении View
    }
}