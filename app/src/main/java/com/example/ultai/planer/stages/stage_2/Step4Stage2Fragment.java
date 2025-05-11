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

public class Step4Stage2Fragment extends Fragment {

    private TextView textViewSalesChannelsTitle;
    private TextView textViewSalesChannelsDescription;
    private EditText editTextSalesChannels;
    private Button buttonFinishStep4Stage2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step_stage2_4, container, false);

        // Инициализация компонентов UI
        initializeUI(view);
        
        // Загрузка сохраненных данных (если есть)
        loadSavedData();

        // Настройка кнопки завершения шага
        buttonFinishStep4Stage2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Данные о каналах продаж сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step4Stage2Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        textViewSalesChannelsTitle = view.findViewById(R.id.textViewSalesChannelsTitle);
        textViewSalesChannelsDescription = view.findViewById(R.id.textViewSalesChannelsDescription);
        editTextSalesChannels = view.findViewById(R.id.editTextSalesChannels);
        buttonFinishStep4Stage2 = view.findViewById(R.id.buttonFinishStep4Stage2);
        
        // Устанавливаем заголовок и описание
        textViewSalesChannelsTitle.setText("Каковы основные каналы продаж?");
        textViewSalesChannelsDescription.setText("Укажите основные каналы, через которые вы продаете товары или услуги.");
    }
    
    /**
     * Загрузка сохраненных данных
     */
    private void loadSavedData() {
        // Получаем данные из SharedPreferences или другого хранилища
        SharedPreferences prefs = requireContext().getSharedPreferences("sales_channels_data", Context.MODE_PRIVATE);
        String salesChannelsData = prefs.getString("sales_channels_info", "");
        
        // Устанавливаем данные в поле ввода
        if (!salesChannelsData.isEmpty()) {
            editTextSalesChannels.setText(salesChannelsData);
        }
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка заполнения поля с информацией о каналах продаж
        if (editTextSalesChannels.getText().toString().trim().isEmpty()) {
            editTextSalesChannels.setError("Пожалуйста, укажите информацию о каналах продаж");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем данные из поля ввода
        String salesChannelsInfo = editTextSalesChannels.getText().toString().trim();
        
        // Сохраняем данные в SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("sales_channels_data", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("sales_channels_info", salesChannelsInfo)
            .apply();
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences stepsPrefs = requireContext().getSharedPreferences("steps_completion_stage2", Context.MODE_PRIVATE);
        stepsPrefs.edit()
            .putBoolean("stage2_step4_completed", true)
            .apply();
    }
} 