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

public class Step6Stage3Fragment extends Fragment {

    private TextView textViewCompetitiveChangesTitle;
    private TextView textViewCompetitiveChangesDescription;
    private EditText editTextCompetitiveChanges;
    private Button buttonFinishStep6Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_6, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep6Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные об изменениях в конкурентной среде сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step6Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewCompetitiveChangesTitle = view.findViewById(R.id.textViewCompetitiveChangesTitle);
        textViewCompetitiveChangesDescription = view.findViewById(R.id.textViewCompetitiveChangesDescription);
        editTextCompetitiveChanges = view.findViewById(R.id.editTextCompetitiveChanges);
        buttonFinishStep6Stage3 = view.findViewById(R.id.buttonFinishStep6Stage3);
        
        // Устанавливаем заголовок и описание
        textViewCompetitiveChangesTitle.setText("Какие изменения в конкурентной среде произошли за последнее время?");
        textViewCompetitiveChangesDescription.setText("Опишите недавние изменения в конкурентной среде, появление новых игроков и тенденции.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("competitive_changes_data", Context.MODE_PRIVATE);
        String competitiveChangesData = prefs.getString("competitive_changes_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!competitiveChangesData.isEmpty()) {
            editTextCompetitiveChanges.setText(competitiveChangesData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией об изменениях в конкурентной среде
        if (editTextCompetitiveChanges.getText().toString().trim().isEmpty()) {
            editTextCompetitiveChanges.setError("Пожалуйста, укажите информацию об изменениях в конкурентной среде");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String competitiveChangesInfo = editTextCompetitiveChanges.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("competitive_changes_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("competitive_changes_info", competitiveChangesInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step6_completed", true)
            .apply();
    }
} 