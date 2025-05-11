package com.example.ultai.planer.stages.stage_2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;

public class Step5Stage2Fragment extends Fragment {

    private TextView textViewResourcesTitle;
    private TextView textViewResourcesDescription;
    private EditText editTextResources;
    private Button buttonFinishStep5Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_5, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep5Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о ресурсах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step5Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewResourcesTitle = view.findViewById(R.id.textViewResourcesTitle);
        textViewResourcesDescription = view.findViewById(R.id.textViewResourcesDescription);
        editTextResources = view.findViewById(R.id.editTextResources);
        buttonFinishStep5Stage2 = view.findViewById(R.id.buttonFinishStep5Stage2);
        
        // Устанавливаем заголовок и описание
        textViewResourcesTitle.setText("Какие ресурсы доступны вам для реализации планов по увеличению продаж?");
        textViewResourcesDescription.setText("Укажите финансовые, человеческие, материальные и другие ресурсы, которые у вас есть.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("resources_data", Context.MODE_PRIVATE);
        String resourcesData = prefs.getString("resources_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!resourcesData.isEmpty()) {
            editTextResources.setText(resourcesData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о ресурсах
        if (editTextResources.getText().toString().trim().isEmpty()) {
            editTextResources.setError("Пожалуйста, укажите информацию о ресурсах");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String resourcesInfo = editTextResources.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("resources_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("resources_info", resourcesInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step5_completed", true)
            .apply();
    }
} 
 

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;

public class Step5Stage2Fragment extends Fragment {

    private TextView textViewResourcesTitle;
    private TextView textViewResourcesDescription;
    private EditText editTextResources;
    private Button buttonFinishStep5Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_5, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep5Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о ресурсах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step5Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewResourcesTitle = view.findViewById(R.id.textViewResourcesTitle);
        textViewResourcesDescription = view.findViewById(R.id.textViewResourcesDescription);
        editTextResources = view.findViewById(R.id.editTextResources);
        buttonFinishStep5Stage2 = view.findViewById(R.id.buttonFinishStep5Stage2);
        
        // Устанавливаем заголовок и описание
        textViewResourcesTitle.setText("Какие ресурсы доступны вам для реализации планов по увеличению продаж?");
        textViewResourcesDescription.setText("Укажите финансовые, человеческие, материальные и другие ресурсы, которые у вас есть.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("resources_data", Context.MODE_PRIVATE);
        String resourcesData = prefs.getString("resources_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!resourcesData.isEmpty()) {
            editTextResources.setText(resourcesData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о ресурсах
        if (editTextResources.getText().toString().trim().isEmpty()) {
            editTextResources.setError("Пожалуйста, укажите информацию о ресурсах");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String resourcesInfo = editTextResources.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("resources_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("resources_info", resourcesInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step5_completed", true)
            .apply();
    }
} 
 