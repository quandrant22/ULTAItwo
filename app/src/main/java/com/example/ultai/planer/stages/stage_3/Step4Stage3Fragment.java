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

public class Step4Stage3Fragment extends Fragment {

    private TextView textViewCompetitorsMarketingTitle;
    private TextView textViewCompetitorsMarketingDescription;
    private EditText editTextCompetitorsMarketing;
    private Button buttonFinishStep4Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_4, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep4Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о маркетинговых стратегиях конкурентов сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step4Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewCompetitorsMarketingTitle = view.findViewById(R.id.textViewCompetitorsMarketingTitle);
        textViewCompetitorsMarketingDescription = view.findViewById(R.id.textViewCompetitorsMarketingDescription);
        editTextCompetitorsMarketing = view.findViewById(R.id.editTextCompetitorsMarketing);
        buttonFinishStep4Stage3 = view.findViewById(R.id.buttonFinishStep4Stage3);
        
        // Устанавливаем заголовок и описание
        textViewCompetitorsMarketingTitle.setText("Какие маркетинговые стратегии и каналы привлечения используют конкуренты?");
        textViewCompetitorsMarketingDescription.setText("Опишите маркетинговые подходы и каналы привлечения, которые используют ваши конкуренты.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_marketing_data", Context.MODE_PRIVATE);
        String competitorsMarketingData = prefs.getString("competitors_marketing_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!competitorsMarketingData.isEmpty()) {
            editTextCompetitorsMarketing.setText(competitorsMarketingData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о маркетинговых стратегиях конкурентов
        if (editTextCompetitorsMarketing.getText().toString().trim().isEmpty()) {
            editTextCompetitorsMarketing.setError("Пожалуйста, укажите информацию о маркетинговых стратегиях конкурентов");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String competitorsMarketingInfo = editTextCompetitorsMarketing.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_marketing_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("competitors_marketing_info", competitorsMarketingInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step4_completed", true)
            .apply();
    }
} 