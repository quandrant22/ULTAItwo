package com.example.ultai.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;
import android.util.Log;

import com.example.ultai20.R;
import com.example.ultai20.databinding.FragmentProfileBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.example.ultai.data.repository.UserRepository;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class); // Используем requireActivity() для ViewModel
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        setupUI();
        setupObservers();
        setupListeners();

        // Загружаем данные при создании или обновлении view
        // viewModel.loadAllProfileData(); // Вызывается в конструкторе ViewModel, но можно добавить кнопку Refresh
    }

    private void setupUI() {
        // Кнопки Назад и Настройки
        binding.imageButton.setOnClickListener(v -> navController.popBackStack());
        binding.imageView19.setOnClickListener(v -> navController.navigate(R.id.action_profileFragment_to_settingsFragment));
        // Кнопка Редактировать/Сохранить
        binding.imageView20.setOnClickListener(v -> viewModel.toggleEditMode());
    }

    private void setupObservers() {
        // Наблюдение за FirebaseUser
        viewModel.getFirebaseUserLiveData().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null) {
                 binding.editTextTextEmailAddress.setText(firebaseUser.getEmail());
                 if (firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().isEmpty()) {
                    binding.textView29.setText(firebaseUser.getDisplayName()); // Имя в заголовке
                    binding.editTextTextName.setText(firebaseUser.getDisplayName()); // Имя в поле редактирования
                 }
            } else {
                 binding.textView29.setText(R.string.label_guest);
                 binding.editTextTextName.setText("");
                 binding.editTextTextEmailAddress.setText("");
                 binding.editTextPhone2.setText("");
                 // Очистить поля анкеты, если они будут добавлены
                 // binding.companyNameTextView.setText(""); 
                 // binding.activityTypeTextView.setText(""); 
            }
        });

        // Наблюдение за объединенным профилем (включая анкету)
        viewModel.getCombinedUserProfileLiveData().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                // Обновляем поля, используя данные из UserProfile
                if (userProfile.getName() != null) { // Имя может быть из узла /profile
                     binding.textView29.setText(userProfile.getName());
                     binding.editTextTextName.setText(userProfile.getName());
                }
                binding.editTextPhone2.setText(userProfile.getPhone() != null ? userProfile.getPhone() : "");

                // --- ОТОБРАЖЕНИЕ ДАННЫХ АНКЕТЫ --- 
                // TODO: Добавьте TextView в ваш XML и замените ID здесь
                // if (binding.companyNameTextView != null) {
                //      binding.companyNameTextView.setText(userProfile.getCompanyName() != null ? userProfile.getCompanyName() : "");
                // }
                // if (binding.activityTypeTextView != null) {
                //      binding.activityTypeTextView.setText(userProfile.getActivityType() != null ? userProfile.getActivityType() : "");
                // }
                // if (binding.productsServicesDescTextView != null) {
                //     binding.productsServicesDescTextView.setText(userProfile.getProductsServicesDescription() != null ? userProfile.getProductsServicesDescription() : "");
                // }
                // ... и так далее для других полей анкеты ...
            }
        });

        // Наблюдение за режимом редактирования
        viewModel.getIsEditModeLiveData().observe(getViewLifecycleOwner(), isEditMode -> {
            setEditMode(isEditMode);
        });

        // Наблюдение за состоянием загрузки
        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Добавьте ProgressBar в XML с ID progressBarProfile
            // if (binding.progressBarProfile != null) { 
            //      binding.progressBarProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // }
        });

        // Наблюдение за сообщениями об ошибках
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG).show();
                // TODO: Реализовать метод clearErrorMessage() в ViewModel, если нужно очищать ошибку после показа
                // viewModel.clearErrorMessage(); 
            }
        });
    }

    private void setEditMode(boolean isEditMode) {
         binding.editTextTextName.setEnabled(isEditMode);
         binding.editTextPhone2.setEnabled(isEditMode);
         // Email обычно не редактируется
         // binding.editTextTextEmailAddress.setEnabled(isEditMode); 

         // --- РЕДАКТИРОВАНИЕ ПОЛЕЙ АНКЕТЫ --- 
         // TODO: Если вы добавите EditText для данных анкеты, включите/выключите их здесь
         // if (binding.companyNameEditText != null) binding.companyNameEditText.setEnabled(isEditMode); 
         // if (binding.activityTypeEditText != null) binding.activityTypeEditText.setEnabled(isEditMode); 

         // Изменяем внешний вид полей
         int backgroundResId = isEditMode ? R.drawable.rounded_rectangle_editable : R.drawable.rounded_rectangle3;
         binding.editTextTextName.setBackgroundResource(backgroundResId);
         binding.editTextPhone2.setBackgroundResource(backgroundResId);
         // TODO: Изменить фон для EditText анкеты, если они есть
         // if (binding.companyNameEditText != null) binding.companyNameEditText.setBackgroundResource(backgroundResId); 
         // if (binding.activityTypeEditText != null) binding.activityTypeEditText.setBackgroundResource(backgroundResId); 

         // Меняем иконку кнопки редактирования/сохранения
         binding.imageView20.setImageResource(isEditMode ? R.drawable.icon_save : R.drawable.redactirovanie);

         // Если выключаем режим редактирования - сохраняем данные
         if (!isEditMode) {
             saveProfileDataIfChanged();
         }
    }

    private void setupListeners() {
        // Обработка системной кнопки "Назад"
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Boolean isEdit = viewModel.getIsEditModeLiveData().getValue();
                if (Boolean.TRUE.equals(isEdit)) {
                    viewModel.toggleEditMode(); // Выходим из режима редактирования
                } else {
                    if (!navController.popBackStack()) { // Пытаемся вернуться по стеку навигации
                         // Если стек пуст, можно либо закрыть активити, либо ничего не делать
                         // requireActivity().finish(); 
                    }
                }
            }
        });
    }

    private void saveProfileDataIfChanged() {
        ProfileViewModel.UserProfile currentCombinedProfile = viewModel.getCombinedUserProfileLiveData().getValue();
        FirebaseUser currentFirebaseUser = viewModel.getFirebaseUserLiveData().getValue();

        if (currentCombinedProfile == null) {
             Log.w("ProfileFragment", "Cannot save profile, currentCombinedProfile is null");
             return; 
        }

        // Собираем новые данные из полей ввода
        String newName = binding.editTextTextName.getText().toString();
        String newPhone = binding.editTextPhone2.getText().toString();
        
        // --- СБОР ДАННЫХ АНКЕТЫ ДЛЯ СОХРАНЕНИЯ --- 
        // TODO: Получите данные из ваших EditText для анкеты
        String newCompanyName = ""; // = binding.companyNameEditText.getText().toString();
        String newActivityType = ""; // = binding.activityTypeEditText.getText().toString();
        String newProductsDesc = ""; // = binding.productsServicesDescEditText.getText().toString();

        // Создаем объект с обновленными данными
        ProfileViewModel.UserProfile updatedProfileData = new ProfileViewModel.UserProfile();
        if (currentFirebaseUser != null) {
            updatedProfileData.setUid(currentFirebaseUser.getUid());
            updatedProfileData.setEmail(currentFirebaseUser.getEmail()); // Email не редактируем
        }
        updatedProfileData.setName(newName);
        updatedProfileData.setPhone(newPhone);
        // TODO: Установите обновленные данные анкеты
        updatedProfileData.setCompanyName(newCompanyName);
        updatedProfileData.setActivityType(newActivityType);
        updatedProfileData.setProductsServicesDescription(newProductsDesc);
        
        // Устанавливаем остальные поля из текущего профиля (которые не редактировались)
        updatedProfileData.setGender(currentCombinedProfile.getGender()); 
        // ... скопируйте другие нередактируемые поля из currentCombinedProfile в updatedProfileData

        // Сравниваем старые и новые данные
        boolean nameChanged = !stringsEqual(newName, currentCombinedProfile.getName());
        boolean phoneChanged = !stringsEqual(newPhone, currentCombinedProfile.getPhone());
        // TODO: Добавьте сравнение для полей анкеты
        boolean companyChanged = !stringsEqual(newCompanyName, currentCombinedProfile.getCompanyName());
        boolean activityChanged = !stringsEqual(newActivityType, currentCombinedProfile.getActivityType());
        // boolean productsDescChanged = !stringsEqual(newProductsDesc, currentCombinedProfile.getProductsServicesDescription());

        boolean changed = nameChanged || phoneChanged || companyChanged || activityChanged; // || productsDescChanged;

        if (changed) {
            Log.d("ProfileFragment", "Profile data changed, saving...");
            viewModel.saveUserProfile(updatedProfileData); // Вызываем сохранение в ViewModel
            // TODO: Убедитесь, что строка R.string.profile_saving_progress существует
            // Snackbar.make(binding.getRoot(), R.string.profile_saving_progress, Snackbar.LENGTH_SHORT).show();
        } else {
            Log.d("ProfileFragment", "Profile data not changed, skipping save.");
        }
    }

    // Вспомогательный метод для сравнения строк, учитывая null
    private boolean stringsEqual(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equals(s2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}