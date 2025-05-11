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

public class Step3Stage2Fragment extends Fragment {

    private TextView textViewPricingTitle;
    private TextView textViewPricingDescription;
    private EditText editTextPricing;
    private Button buttonFinishStep3Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_3, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep3Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о ценах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step3Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewPricingTitle = view.findViewById(R.id.textViewPricingTitle);
        textViewPricingDescription = view.findViewById(R.id.textViewPricingDescription);
        editTextPricing = view.findViewById(R.id.editTextPricing);
        buttonFinishStep3Stage2 = view.findViewById(R.id.buttonFinishStep3Stage2);
        
        // Устанавливаем заголовок и описание
        textViewPricingTitle.setText("Каковы текущие цены на ваши продукты или услуги?");
        textViewPricingDescription.setText("Укажите текущие цены на ваши основные продукты и услуги.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("pricing_data", Context.MODE_PRIVATE);
        String pricingData = prefs.getString("pricing_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!pricingData.isEmpty()) {
            editTextPricing.setText(pricingData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о ценах
        if (editTextPricing.getText().toString().trim().isEmpty()) {
            editTextPricing.setError("Пожалуйста, укажите информацию о ценах");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String pricingInfo = editTextPricing.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("pricing_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("pricing_info", pricingInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step3_completed", true)
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

public class Step3Stage2Fragment extends Fragment {

    private TextView textViewPricingTitle;
    private TextView textViewPricingDescription;
    private EditText editTextPricing;
    private Button buttonFinishStep3Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_3, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep3Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о ценах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step3Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewPricingTitle = view.findViewById(R.id.textViewPricingTitle);
        textViewPricingDescription = view.findViewById(R.id.textViewPricingDescription);
        editTextPricing = view.findViewById(R.id.editTextPricing);
        buttonFinishStep3Stage2 = view.findViewById(R.id.buttonFinishStep3Stage2);
        
        // Устанавливаем заголовок и описание
        textViewPricingTitle.setText("Каковы текущие цены на ваши продукты или услуги?");
        textViewPricingDescription.setText("Укажите текущие цены на ваши основные продукты и услуги.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("pricing_data", Context.MODE_PRIVATE);
        String pricingData = prefs.getString("pricing_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!pricingData.isEmpty()) {
            editTextPricing.setText(pricingData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о ценах
        if (editTextPricing.getText().toString().trim().isEmpty()) {
            editTextPricing.setError("Пожалуйста, укажите информацию о ценах");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String pricingInfo = editTextPricing.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("pricing_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("pricing_info", pricingInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step3_completed", true)
            .apply();
    }
} 
 