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

public class Step6Stage2Fragment extends Fragment {

    private TextView textViewFinancialTitle;
    private TextView textViewFinancialDescription;
    private EditText editTextFinancial;
    private Button buttonFinishStep6Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_6, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep6Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о доходах и расходах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step6Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewFinancialTitle = view.findViewById(R.id.textViewFinancialTitle);
        textViewFinancialDescription = view.findViewById(R.id.textViewFinancialDescription);
        editTextFinancial = view.findViewById(R.id.editTextFinancial);
        buttonFinishStep6Stage2 = view.findViewById(R.id.buttonFinishStep6Stage2);
        
        // Устанавливаем заголовок и описание
        textViewFinancialTitle.setText("Какова общая структура доходов и расходов?");
        textViewFinancialDescription.setText("Опишите основные источники доходов и главные статьи расходов.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("financial_data", Context.MODE_PRIVATE);
        String financialData = prefs.getString("financial_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!financialData.isEmpty()) {
            editTextFinancial.setText(financialData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о доходах и расходах
        if (editTextFinancial.getText().toString().trim().isEmpty()) {
            editTextFinancial.setError("Пожалуйста, укажите информацию о доходах и расходах");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String financialInfo = editTextFinancial.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("financial_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("financial_info", financialInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step6_completed", true)
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

public class Step6Stage2Fragment extends Fragment {

    private TextView textViewFinancialTitle;
    private TextView textViewFinancialDescription;
    private EditText editTextFinancial;
    private Button buttonFinishStep6Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_6, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep6Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о доходах и расходах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step6Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewFinancialTitle = view.findViewById(R.id.textViewFinancialTitle);
        textViewFinancialDescription = view.findViewById(R.id.textViewFinancialDescription);
        editTextFinancial = view.findViewById(R.id.editTextFinancial);
        buttonFinishStep6Stage2 = view.findViewById(R.id.buttonFinishStep6Stage2);
        
        // Устанавливаем заголовок и описание
        textViewFinancialTitle.setText("Какова общая структура доходов и расходов?");
        textViewFinancialDescription.setText("Опишите основные источники доходов и главные статьи расходов.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("financial_data", Context.MODE_PRIVATE);
        String financialData = prefs.getString("financial_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!financialData.isEmpty()) {
            editTextFinancial.setText(financialData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о доходах и расходах
        if (editTextFinancial.getText().toString().trim().isEmpty()) {
            editTextFinancial.setError("Пожалуйста, укажите информацию о доходах и расходах");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String financialInfo = editTextFinancial.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("financial_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("financial_info", financialInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step6_completed", true)
            .apply();
    }
}
 