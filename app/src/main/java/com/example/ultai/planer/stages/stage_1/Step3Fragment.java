package com.example.ultai.planer.stages.stage_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;

public class Step3Fragment extends Fragment {

    private CheckBox checkBoxEmotional;
    private CheckBox checkBoxRational;
    private CheckBox checkBoxImpulsive;
    private CheckBox checkBoxValueSeeking;
    private CheckBox checkBoxSociallyInfluenced;
    private EditText editTextOtherMotivation;
    private Button buttonFinishStep3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step3, container, false);

        // Инициализация компонентов UI
        initializeUI(view);

        // Настройка кнопки завершения шага
        buttonFinishStep3.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Психологические характеристики сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step3Fragment_to_faza1_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        checkBoxEmotional = view.findViewById(R.id.checkBoxEmotional);
        checkBoxRational = view.findViewById(R.id.checkBoxRational);
        checkBoxImpulsive = view.findViewById(R.id.checkBoxImpulsive);
        checkBoxValueSeeking = view.findViewById(R.id.checkBoxValueSeeking);
        checkBoxSociallyInfluenced = view.findViewById(R.id.checkBoxSociallyInfluenced);
        editTextOtherMotivation = view.findViewById(R.id.editTextOtherMotivation);
        buttonFinishStep3 = view.findViewById(R.id.buttonFinishStep3);
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка выбора хотя бы одной психологической характеристики
        if (!checkBoxEmotional.isChecked() && !checkBoxRational.isChecked() && 
            !checkBoxImpulsive.isChecked() && !checkBoxValueSeeking.isChecked() && 
            !checkBoxSociallyInfluenced.isChecked()) {
            Toast.makeText(requireContext(), "Выберите хотя бы одну психологическую характеристику", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Собираем данные о психологических характеристиках
        StringBuilder psychologicalTraits = new StringBuilder();
        if (checkBoxEmotional.isChecked()) psychologicalTraits.append("Эмоциональные покупатели, ");
        if (checkBoxRational.isChecked()) psychologicalTraits.append("Рациональные покупатели, ");
        if (checkBoxImpulsive.isChecked()) psychologicalTraits.append("Импульсивные покупатели, ");
        if (checkBoxValueSeeking.isChecked()) psychologicalTraits.append("Ищущие ценность покупатели, ");
        if (checkBoxSociallyInfluenced.isChecked()) psychologicalTraits.append("Социально-зависимые покупатели, ");
        
        // Получаем дополнительные мотивации
        String otherMotivation = editTextOtherMotivation.getText().toString().trim();
        
        // В реальном приложении здесь сохраняем данные в ViewModel или БД
        // Например:
        // StepViewModel viewModel = new ViewModelProvider(requireActivity()).get(StepViewModel.class);
        // viewModel.saveStep3Data(psychologicalTraits.toString(), otherMotivation);
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("steps_completion", Context.MODE_PRIVATE);
        sharedPreferences.edit()
            .putBoolean("step3_completed", true)
            .apply();
    }
} 