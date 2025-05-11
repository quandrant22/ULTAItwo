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

import com.example.ultai20.R;

public class Step1Stage3Fragment extends Fragment {

    private TextView textViewCompetitorsTitle;
    private TextView textViewCompetitorsDescription;
    private EditText editTextCompetitors;
    private Button buttonFinishStep1Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_1, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep1Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о конкурентах сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step1Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewCompetitorsTitle = view.findViewById(R.id.textViewCompetitorsTitle);
        textViewCompetitorsDescription = view.findViewById(R.id.textViewCompetitorsDescription);
        editTextCompetitors = view.findViewById(R.id.editTextCompetitors);
        buttonFinishStep1Stage3 = view.findViewById(R.id.buttonFinishStep1Stage3);
        
        // Устанавливаем заголовок и описание
        textViewCompetitorsTitle.setText("Кто является вашими основными конкурентами на рынке?");
        textViewCompetitorsDescription.setText("Перечислите основных конкурентов вашего бизнеса на рынке.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_data", Context.MODE_PRIVATE);
        String competitorsData = prefs.getString("competitors_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!competitorsData.isEmpty()) {
            editTextCompetitors.setText(competitorsData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о конкурентах
        if (editTextCompetitors.getText().toString().trim().isEmpty()) {
            editTextCompetitors.setError("Пожалуйста, укажите информацию о конкурентах");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String competitorsInfo = editTextCompetitors.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("competitors_info", competitorsInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step1_completed", true)
            .apply();
    }
} 