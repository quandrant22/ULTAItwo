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

import com.example.ultai20.R;

public class Step2Stage2Fragment extends Fragment {

    private TextView textViewSalesVolumeTitle;
    private TextView textViewSalesVolumeDescription;
    private EditText editTextSalesVolume;
    private Button buttonFinishStep2Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_2, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка данных из анкеты PLANNER (если есть)
        loadDataFromPlanner();

        // Настройка кнопки завершения шага
        buttonFinishStep2Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные об объемах продаж сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step2Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewSalesVolumeTitle = view.findViewById(R.id.textViewSalesVolumeTitle);
        textViewSalesVolumeDescription = view.findViewById(R.id.textViewSalesVolumeDescription);
        editTextSalesVolume = view.findViewById(R.id.editTextSalesVolume);
        buttonFinishStep2Stage2 = view.findViewById(R.id.buttonFinishStep2Stage2);
        
        // Устанавливаем заголовок и описание
        textViewSalesVolumeTitle.setText("Каковы текущие объемы продаж продуктов/услуг?");
        textViewSalesVolumeDescription.setText("Информация подтягивается из анкеты PLANNER. Вы можете изменить ответ.");
    }
    
    /**
     * Загрузка данных из анкеты PLANNER
     */
    private void loadDataFromPlanner() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("planner_data", Context.MODE_PRIVATE);
        String salesVolumeData = prefs.getString("sales_volume", "");
        
        // Устанавливаем данные в поле ввода
        if (!salesVolumeData.isEmpty()) {
            editTextSalesVolume.setText(salesVolumeData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с объемами продаж
        if (editTextSalesVolume.getText().toString().trim().isEmpty()) {
            editTextSalesVolume.setError("Пожалуйста, укажите объемы продаж");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String salesVolume = editTextSalesVolume.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("planner_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("sales_volume", salesVolume)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step2_completed", true)
            .apply();
    }
} 