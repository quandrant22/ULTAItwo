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

import com.example.ultai20.R;

public class Step1Stage2Fragment extends Fragment {

    private TextView textViewProductsServicesTitle;
    private TextView textViewProductsServicesDescription;
    private EditText editTextProductsServices;
    private Button buttonFinishStep1Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_1, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка данных из первой анкеты (если есть)
        loadDataFromFirstQuestionnaire();

        // Настройка кнопки завершения шага
        buttonFinishStep1Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о продуктах и услугах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step1Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewProductsServicesTitle = view.findViewById(R.id.textViewProductsServicesTitle);
        textViewProductsServicesDescription = view.findViewById(R.id.textViewProductsServicesDescription);
        editTextProductsServices = view.findViewById(R.id.editTextProductsServices);
        buttonFinishStep1Stage2 = view.findViewById(R.id.buttonFinishStep1Stage2);
        
        // Устанавливаем заголовок и описание
        textViewProductsServicesTitle.setText("Какие продукты или услуги вы предлагаете?");
        textViewProductsServicesDescription.setText("Информация подтягивается из первой анкеты. Вы можете изменить ответ.");
    }
    
    /**
     * Загрузка данных из первой анкеты
     */
    private void loadDataFromFirstQuestionnaire() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("questionnaire_data", Context.MODE_PRIVATE);
        String productsServicesData = prefs.getString("products_services", "");
        
        // Устанавливаем данные в поле ввода
        if (!productsServicesData.isEmpty()) {
            editTextProductsServices.setText(productsServicesData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с продуктами/услугами
        if (editTextProductsServices.getText().toString().trim().isEmpty()) {
            editTextProductsServices.setError("Пожалуйста, укажите предлагаемые продукты или услуги");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String productsServices = editTextProductsServices.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("questionnaire_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("products_services", productsServices)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step1_completed", true)
            .apply();
    }
} 