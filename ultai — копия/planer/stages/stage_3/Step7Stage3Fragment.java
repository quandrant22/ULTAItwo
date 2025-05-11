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

public class Step7Stage3Fragment extends Fragment {

    private TextView textViewCollaborationTitle;
    private TextView textViewCollaborationDescription;
    private EditText editTextCollaboration;
    private Button buttonFinishStep7Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_7, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep7Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о возможностях сотрудничества сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step7Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewCollaborationTitle = view.findViewById(R.id.textViewCollaborationTitle);
        textViewCollaborationDescription = view.findViewById(R.id.textViewCollaborationDescription);
        editTextCollaboration = view.findViewById(R.id.editTextCollaboration);
        buttonFinishStep7Stage3 = view.findViewById(R.id.buttonFinishStep7Stage3);
        
        // Устанавливаем заголовок и описание
        textViewCollaborationTitle.setText("Какие возможности для сотрудничества с конкурентами вы видите?");
        textViewCollaborationDescription.setText("Опишите потенциальные возможности для сотрудничества с конкурентами и другими участниками рынка.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("collaboration_data", Context.MODE_PRIVATE);
        String collaborationData = prefs.getString("collaboration_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!collaborationData.isEmpty()) {
            editTextCollaboration.setText(collaborationData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о возможностях сотрудничества
        if (editTextCollaboration.getText().toString().trim().isEmpty()) {
            editTextCollaboration.setError("Пожалуйста, укажите информацию о возможностях сотрудничества");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String collaborationInfo = editTextCollaboration.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("collaboration_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("collaboration_info", collaborationInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step7_completed", true)
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

public class Step7Stage3Fragment extends Fragment {

    private TextView textViewCollaborationTitle;
    private TextView textViewCollaborationDescription;
    private EditText editTextCollaboration;
    private Button buttonFinishStep7Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_7, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep7Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о возможностях сотрудничества сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step7Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewCollaborationTitle = view.findViewById(R.id.textViewCollaborationTitle);
        textViewCollaborationDescription = view.findViewById(R.id.textViewCollaborationDescription);
        editTextCollaboration = view.findViewById(R.id.editTextCollaboration);
        buttonFinishStep7Stage3 = view.findViewById(R.id.buttonFinishStep7Stage3);
        
        // Устанавливаем заголовок и описание
        textViewCollaborationTitle.setText("Какие возможности для сотрудничества с конкурентами вы видите?");
        textViewCollaborationDescription.setText("Опишите потенциальные возможности для сотрудничества с конкурентами и другими участниками рынка.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("collaboration_data", Context.MODE_PRIVATE);
        String collaborationData = prefs.getString("collaboration_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!collaborationData.isEmpty()) {
            editTextCollaboration.setText(collaborationData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о возможностях сотрудничества
        if (editTextCollaboration.getText().toString().trim().isEmpty()) {
            editTextCollaboration.setError("Пожалуйста, укажите информацию о возможностях сотрудничества");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String collaborationInfo = editTextCollaboration.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("collaboration_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("collaboration_info", collaborationInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step7_completed", true)
            .apply();
    }
} 
 