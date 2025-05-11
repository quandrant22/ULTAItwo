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

public class Step7Stage2Fragment extends Fragment {

    private TextView textViewChallengesTitle;
    private TextView textViewChallengesDescription;
    private EditText editTextChallenges;
    private Button buttonFinishStep7Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_7, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep7Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о проблемах и вызовах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step7Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewChallengesTitle = view.findViewById(R.id.textViewChallengesTitle);
        textViewChallengesDescription = view.findViewById(R.id.textViewChallengesDescription);
        editTextChallenges = view.findViewById(R.id.editTextChallenges);
        buttonFinishStep7Stage2 = view.findViewById(R.id.buttonFinishStep7Stage2);
        
        // Устанавливаем заголовок и описание
        textViewChallengesTitle.setText("Какие проблемы или вызовы вы испытываете в настоящее время в своем бизнесе?");
        textViewChallengesDescription.setText("Опишите основные проблемы и трудности, с которыми сталкивается ваш бизнес.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("challenges_data", Context.MODE_PRIVATE);
        String challengesData = prefs.getString("challenges_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!challengesData.isEmpty()) {
            editTextChallenges.setText(challengesData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о проблемах и вызовах
        if (editTextChallenges.getText().toString().trim().isEmpty()) {
            editTextChallenges.setError("Пожалуйста, укажите информацию о проблемах и вызовах");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String challengesInfo = editTextChallenges.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("challenges_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("challenges_info", challengesInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step7_completed", true)
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

public class Step7Stage2Fragment extends Fragment {

    private TextView textViewChallengesTitle;
    private TextView textViewChallengesDescription;
    private EditText editTextChallenges;
    private Button buttonFinishStep7Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_7, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep7Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о проблемах и вызовах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step7Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewChallengesTitle = view.findViewById(R.id.textViewChallengesTitle);
        textViewChallengesDescription = view.findViewById(R.id.textViewChallengesDescription);
        editTextChallenges = view.findViewById(R.id.editTextChallenges);
        buttonFinishStep7Stage2 = view.findViewById(R.id.buttonFinishStep7Stage2);
        
        // Устанавливаем заголовок и описание
        textViewChallengesTitle.setText("Какие проблемы или вызовы вы испытываете в настоящее время в своем бизнесе?");
        textViewChallengesDescription.setText("Опишите основные проблемы и трудности, с которыми сталкивается ваш бизнес.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("challenges_data", Context.MODE_PRIVATE);
        String challengesData = prefs.getString("challenges_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!challengesData.isEmpty()) {
            editTextChallenges.setText(challengesData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о проблемах и вызовах
        if (editTextChallenges.getText().toString().trim().isEmpty()) {
            editTextChallenges.setError("Пожалуйста, укажите информацию о проблемах и вызовах");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String challengesInfo = editTextChallenges.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("challenges_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("challenges_info", challengesInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step7_completed", true)
            .apply();
    }
} 
 