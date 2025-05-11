package com.example.ultai.planer.stages.stage_3;

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

public class Step3Stage3Fragment extends Fragment {

    private TextView textViewCompetitorsStrengthsTitle;
    private TextView textViewCompetitorsStrengthsDescription;
    private EditText editTextCompetitorsStrengths;
    private Button buttonFinishStep3Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_3, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep3Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о сильных и слабых сторонах конкурентов сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step3Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewCompetitorsStrengthsTitle = view.findViewById(R.id.textViewCompetitorsStrengthsTitle);
        textViewCompetitorsStrengthsDescription = view.findViewById(R.id.textViewCompetitorsStrengthsDescription);
        editTextCompetitorsStrengths = view.findViewById(R.id.editTextCompetitorsStrengths);
        buttonFinishStep3Stage3 = view.findViewById(R.id.buttonFinishStep3Stage3);
        
        // Устанавливаем заголовок и описание
        textViewCompetitorsStrengthsTitle.setText("Каковы сильные и слабые стороны конкурентов, их репутация на рынке?");
        textViewCompetitorsStrengthsDescription.setText("Опишите преимущества, недостатки и репутацию ваших конкурентов.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_strengths_data", Context.MODE_PRIVATE);
        String competitorsStrengthsData = prefs.getString("competitors_strengths_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!competitorsStrengthsData.isEmpty()) {
            editTextCompetitorsStrengths.setText(competitorsStrengthsData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о сильных и слабых сторонах конкурентов
        if (editTextCompetitorsStrengths.getText().toString().trim().isEmpty()) {
            editTextCompetitorsStrengths.setError("Пожалуйста, укажите информацию о сильных и слабых сторонах конкурентов");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String competitorsStrengthsInfo = editTextCompetitorsStrengths.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_strengths_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("competitors_strengths_info", competitorsStrengthsInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step3_completed", true)
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

public class Step3Stage3Fragment extends Fragment {

    private TextView textViewCompetitorsStrengthsTitle;
    private TextView textViewCompetitorsStrengthsDescription;
    private EditText editTextCompetitorsStrengths;
    private Button buttonFinishStep3Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_3, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep3Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о сильных и слабых сторонах конкурентов сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step3Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewCompetitorsStrengthsTitle = view.findViewById(R.id.textViewCompetitorsStrengthsTitle);
        textViewCompetitorsStrengthsDescription = view.findViewById(R.id.textViewCompetitorsStrengthsDescription);
        editTextCompetitorsStrengths = view.findViewById(R.id.editTextCompetitorsStrengths);
        buttonFinishStep3Stage3 = view.findViewById(R.id.buttonFinishStep3Stage3);
        
        // Устанавливаем заголовок и описание
        textViewCompetitorsStrengthsTitle.setText("Каковы сильные и слабые стороны конкурентов, их репутация на рынке?");
        textViewCompetitorsStrengthsDescription.setText("Опишите преимущества, недостатки и репутацию ваших конкурентов.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_strengths_data", Context.MODE_PRIVATE);
        String competitorsStrengthsData = prefs.getString("competitors_strengths_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!competitorsStrengthsData.isEmpty()) {
            editTextCompetitorsStrengths.setText(competitorsStrengthsData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о сильных и слабых сторонах конкурентов
        if (editTextCompetitorsStrengths.getText().toString().trim().isEmpty()) {
            editTextCompetitorsStrengths.setError("Пожалуйста, укажите информацию о сильных и слабых сторонах конкурентов");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String competitorsStrengthsInfo = editTextCompetitorsStrengths.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_strengths_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("competitors_strengths_info", competitorsStrengthsInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step3_completed", true)
            .apply();
    }
} 
 