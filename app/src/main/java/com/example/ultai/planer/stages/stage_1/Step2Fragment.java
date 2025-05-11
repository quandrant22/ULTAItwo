package com.example.ultai.planer.stages.stage_1;

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

public class Step2Fragment extends Fragment {

    // Чекбоксы для образования
    private CheckBox checkBoxGeneralEducation;
    private CheckBox checkBoxSecondaryEducation;
    private CheckBox checkBoxHigherEducation;
    private CheckBox checkBoxAdditionalEducation;
    private CheckBox checkBoxProfessionalTraining;
    
    // Текстовые поля для ввода
    private EditText editTextActivitySphere;
    private EditText editTextProfessionalActivity;
    private EditText editTextWorkExperience;
    private EditText editTextPosition;
    
    // Чекбоксы для уровня дохода
    private CheckBox checkBoxLowIncome;
    private CheckBox checkBoxMiddleIncome;
    private CheckBox checkBoxHighIncome;
    
    private Button buttonFinishStep2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step2, container, false);

        // Инициализация компонентов UI
        initializeUI(view);

        // Настройка кнопки завершения шага
        buttonFinishStep2.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Социальные характеристики сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step2Fragment_to_faza1_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        // Инициализация чекбоксов для образования
        checkBoxGeneralEducation = view.findViewById(R.id.checkBoxGeneralEducation);
        checkBoxSecondaryEducation = view.findViewById(R.id.checkBoxSecondaryEducation);
        checkBoxHigherEducation = view.findViewById(R.id.checkBoxHigherEducation);
        checkBoxAdditionalEducation = view.findViewById(R.id.checkBoxAdditionalEducation);
        checkBoxProfessionalTraining = view.findViewById(R.id.checkBoxProfessionalTraining);
        
        // Инициализация текстовых полей
        editTextActivitySphere = view.findViewById(R.id.editTextActivitySphere);
        editTextProfessionalActivity = view.findViewById(R.id.editTextProfessionalActivity);
        editTextWorkExperience = view.findViewById(R.id.editTextWorkExperience);
        editTextPosition = view.findViewById(R.id.editTextPosition);
        
        // Инициализация чекбоксов для уровня дохода
        checkBoxLowIncome = view.findViewById(R.id.checkBoxLowIncome);
        checkBoxMiddleIncome = view.findViewById(R.id.checkBoxMiddleIncome);
        checkBoxHighIncome = view.findViewById(R.id.checkBoxHighIncome);
        
        buttonFinishStep2 = view.findViewById(R.id.buttonFinishStep2);
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка выбора уровня образования
        if (!checkBoxGeneralEducation.isChecked() && 
            !checkBoxSecondaryEducation.isChecked() && 
            !checkBoxHigherEducation.isChecked() &&
            !checkBoxAdditionalEducation.isChecked() &&
            !checkBoxProfessionalTraining.isChecked()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один вариант образования", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка заполнения сферы деятельности
        if (editTextActivitySphere.getText().toString().trim().isEmpty()) {
            editTextActivitySphere.setError("Пожалуйста, укажите сферу деятельности");
            isValid = false;
        }
        
        // Проверка выбора уровня дохода
        if (!checkBoxLowIncome.isChecked() && 
            !checkBoxMiddleIncome.isChecked() && 
            !checkBoxHighIncome.isChecked()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один уровень дохода", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Собираем данные об образовании
        StringBuilder education = new StringBuilder();
        if (checkBoxGeneralEducation.isChecked()) education.append("Общее образование, ");
        if (checkBoxSecondaryEducation.isChecked()) education.append("Среднее профессиональное образование, ");
        if (checkBoxHigherEducation.isChecked()) education.append("Высшее образование, ");
        if (checkBoxAdditionalEducation.isChecked()) education.append("Дополнительное образование, ");
        if (checkBoxProfessionalTraining.isChecked()) education.append("Профессиональное обучение, ");
        
        // Собираем данные о доходе
        StringBuilder income = new StringBuilder();
        if (checkBoxLowIncome.isChecked()) income.append("Низкий доход (менее 30 000 рублей в месяц), ");
        if (checkBoxMiddleIncome.isChecked()) income.append("Средний доход (30 000 - 100 000 рублей в месяц), ");
        if (checkBoxHighIncome.isChecked()) income.append("Высокий доход (более 100 000 рублей в месяц), ");
        
        // Получаем данные из текстовых полей
        String activitySphere = editTextActivitySphere.getText().toString().trim();
        String professionalActivity = editTextProfessionalActivity.getText().toString().trim();
        String workExperience = editTextWorkExperience.getText().toString().trim();
        String position = editTextPosition.getText().toString().trim();
        
        // В реальном приложении здесь сохраняем данные в ViewModel или БД
        // Например:
        // StepViewModel viewModel = new ViewModelProvider(requireActivity()).get(StepViewModel.class);
        // viewModel.saveStep2Data(education.toString(), income.toString(), activitySphere, 
        //                         professionalActivity, workExperience, position);
        
        // Сохраняем в SharedPreferences что шаг выполнен
        requireContext().getSharedPreferences("steps_completion", 0)
            .edit()
            .putBoolean("step2_completed", true)
            .apply();
    }
} 