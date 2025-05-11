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

public class Step2Stage3Fragment extends Fragment {

    private TextView textViewCompetitorsProductsTitle;
    private TextView textViewCompetitorsProductsDescription;
    private EditText editTextCompetitorsProducts;
    private Button buttonFinishStep2Stage3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage3_2, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep2Stage3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о продуктах/услугах конкурентов сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step2Stage3Fragment_to_faza3_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewCompetitorsProductsTitle = view.findViewById(R.id.textViewCompetitorsProductsTitle);
        textViewCompetitorsProductsDescription = view.findViewById(R.id.textViewCompetitorsProductsDescription);
        editTextCompetitorsProducts = view.findViewById(R.id.editTextCompetitorsProducts);
        buttonFinishStep2Stage3 = view.findViewById(R.id.buttonFinishStep2Stage3);
        
        // Устанавливаем заголовок и описание
        textViewCompetitorsProductsTitle.setText("Чем выделяются ваши конкуренты и какие продукты/услуги они предлагают?");
        textViewCompetitorsProductsDescription.setText("Опишите особенности и продукты/услуги ваших конкурентов.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_products_data", Context.MODE_PRIVATE);
        String competitorsProductsData = prefs.getString("competitors_products_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!competitorsProductsData.isEmpty()) {
            editTextCompetitorsProducts.setText(competitorsProductsData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о продуктах/услугах конкурентов
        if (editTextCompetitorsProducts.getText().toString().trim().isEmpty()) {
            editTextCompetitorsProducts.setError("Пожалуйста, укажите информацию о продуктах/услугах конкурентов");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String competitorsProductsInfo = editTextCompetitorsProducts.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("competitors_products_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("competitors_products_info", competitorsProductsInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage3", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage3_step2_completed", true)
            .apply();
    }
} 