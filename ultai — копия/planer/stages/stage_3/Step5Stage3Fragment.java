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

public class Step5Stage3Fragment extends Fragment {

    private TextView textViewUTPTitle;
    private TextView textViewUTPDescription;
    private EditText editTextUTP;
    private Button buttonFinishStep5Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_5, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep5Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о УТП и целевой аудитории сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step5Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewUTPTitle = view.findViewById(R.id.textViewUTPTitle);
        textViewUTPDescription = view.findViewById(R.id.textViewUTPDescription);
        editTextUTP = view.findViewById(R.id.editTextUTP);
        buttonFinishStep5Stage3 = view.findViewById(R.id.buttonFinishStep5Stage3);
        
        // Устанавливаем заголовок и описание
        textViewUTPTitle.setText("В чем заключается ваше уникальное торговое предложение и какова ЦА ваших конкурентов?");
        textViewUTPDescription.setText("Опишите ваше уникальное предложение и целевую аудиторию конкурентов.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("utp_data", Context.MODE_PRIVATE);
        String utpData = prefs.getString("utp_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!utpData.isEmpty()) {
            editTextUTP.setText(utpData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о УТП и целевой аудитории
        if (editTextUTP.getText().toString().trim().isEmpty()) {
            editTextUTP.setError("Пожалуйста, укажите информацию о УТП и целевой аудитории");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String utpInfo = editTextUTP.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("utp_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("utp_info", utpInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step5_completed", true)
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

public class Step5Stage3Fragment extends Fragment {

    private TextView textViewUTPTitle;
    private TextView textViewUTPDescription;
    private EditText editTextUTP;
    private Button buttonFinishStep5Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_5, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep5Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о УТП и целевой аудитории сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step5Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewUTPTitle = view.findViewById(R.id.textViewUTPTitle);
        textViewUTPDescription = view.findViewById(R.id.textViewUTPDescription);
        editTextUTP = view.findViewById(R.id.editTextUTP);
        buttonFinishStep5Stage3 = view.findViewById(R.id.buttonFinishStep5Stage3);
        
        // Устанавливаем заголовок и описание
        textViewUTPTitle.setText("В чем заключается ваше уникальное торговое предложение и какова ЦА ваших конкурентов?");
        textViewUTPDescription.setText("Опишите ваше уникальное предложение и целевую аудиторию конкурентов.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("utp_data", Context.MODE_PRIVATE);
        String utpData = prefs.getString("utp_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!utpData.isEmpty()) {
            editTextUTP.setText(utpData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о УТП и целевой аудитории
        if (editTextUTP.getText().toString().trim().isEmpty()) {
            editTextUTP.setError("Пожалуйста, укажите информацию о УТП и целевой аудитории");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String utpInfo = editTextUTP.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("utp_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("utp_info", utpInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step5_completed", true)
            .apply();
    }
} 
 