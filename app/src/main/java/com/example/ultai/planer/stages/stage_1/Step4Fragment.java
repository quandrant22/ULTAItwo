package com.example.ultai.planer.stages.stage_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;

public class Step4Fragment extends Fragment {

    // Цели и задачи
    private CheckBox checkBoxBasicNeeds;
    private CheckBox checkBoxProblemSolving;
    private CheckBox checkBoxQualityImprovement;
    private CheckBox checkBoxPersonalExpression;
    private CheckBox checkBoxResourceSaving;
    private CheckBox checkBoxPleasure;
    private CheckBox checkBoxSafety;
    
    // Текстовые поля
    private EditText editTextGoalsRelation;
    private EditText editTextProductChoice;
    private EditText editTextUsageBarriers;
    
    // Мотивы
    private CheckBox checkBoxNeedsMotives;
    private CheckBox checkBoxPersonalMotives;
    private CheckBox checkBoxEconomicMotives;
    private CheckBox checkBoxSocialMotives;
    private CheckBox checkBoxPracticalMotives;
    private CheckBox checkBoxPsychologicalMotives;
    
    // Барьеры при покупке
    private CheckBox checkBoxFinancialBarriers;
    private CheckBox checkBoxInfoBarriers;
    private CheckBox checkBoxPriceBarriers;
    private CheckBox checkBoxTrustBarriers;
    private CheckBox checkBoxExperienceBarriers;
    private CheckBox checkBoxTimeBarriers;
    private CheckBox checkBoxSocialBarriers;
    private CheckBox checkBoxNeedsBarriers;
    
    // Отношение к технологиям
    private RadioGroup radioGroupTechAttitude;
    private RadioGroup radioGroupUseTech;
    
    // Устройства и платформы
    private CheckBox checkBoxAndroid;
    private CheckBox checkBoxIOS;
    private CheckBox checkBoxWindows;
    private CheckBox checkBoxLinux;
    private CheckBox checkBoxMacOS;
    
    // Частота обновлений
    private RadioGroup radioGroupUpdateFreq;
    
    private Button buttonFinishStep4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step4, container, false);

        // Инициализация компонентов UI
        initializeUI(view);

        // Настройка кнопки завершения шага
        buttonFinishStep4.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Психографические характеристики сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step4Fragment_to_faza1_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        // Инициализация чекбоксов для целей и задач
        checkBoxBasicNeeds = view.findViewById(R.id.checkBoxBasicNeeds);
        checkBoxProblemSolving = view.findViewById(R.id.checkBoxProblemSolving);
        checkBoxQualityImprovement = view.findViewById(R.id.checkBoxQualityImprovement);
        checkBoxPersonalExpression = view.findViewById(R.id.checkBoxPersonalExpression);
        checkBoxResourceSaving = view.findViewById(R.id.checkBoxResourceSaving);
        checkBoxPleasure = view.findViewById(R.id.checkBoxPleasure);
        checkBoxSafety = view.findViewById(R.id.checkBoxSafety);
        
        // Инициализация текстовых полей
        editTextGoalsRelation = view.findViewById(R.id.editTextGoalsRelation);
        editTextProductChoice = view.findViewById(R.id.editTextProductChoice);
        editTextUsageBarriers = view.findViewById(R.id.editTextUsageBarriers);
        
        // Инициализация чекбоксов для мотивов
        checkBoxNeedsMotives = view.findViewById(R.id.checkBoxNeedsMotives);
        checkBoxPersonalMotives = view.findViewById(R.id.checkBoxPersonalMotives);
        checkBoxEconomicMotives = view.findViewById(R.id.checkBoxEconomicMotives);
        checkBoxSocialMotives = view.findViewById(R.id.checkBoxSocialMotives);
        checkBoxPracticalMotives = view.findViewById(R.id.checkBoxPracticalMotives);
        checkBoxPsychologicalMotives = view.findViewById(R.id.checkBoxPsychologicalMotives);
        
        // Инициализация чекбоксов для барьеров при покупке
        checkBoxFinancialBarriers = view.findViewById(R.id.checkBoxFinancialBarriers);
        checkBoxInfoBarriers = view.findViewById(R.id.checkBoxInfoBarriers);
        checkBoxPriceBarriers = view.findViewById(R.id.checkBoxPriceBarriers);
        checkBoxTrustBarriers = view.findViewById(R.id.checkBoxTrustBarriers);
        checkBoxExperienceBarriers = view.findViewById(R.id.checkBoxExperienceBarriers);
        checkBoxTimeBarriers = view.findViewById(R.id.checkBoxTimeBarriers);
        checkBoxSocialBarriers = view.findViewById(R.id.checkBoxSocialBarriers);
        checkBoxNeedsBarriers = view.findViewById(R.id.checkBoxNeedsBarriers);
        
        // Инициализация радиогрупп для отношения к технологиям
        radioGroupTechAttitude = view.findViewById(R.id.radioGroupTechAttitude);
        radioGroupUseTech = view.findViewById(R.id.radioGroupUseTech);
        
        // Инициализация чекбоксов для устройств и платформ
        checkBoxAndroid = view.findViewById(R.id.checkBoxAndroid);
        checkBoxIOS = view.findViewById(R.id.checkBoxIOS);
        checkBoxWindows = view.findViewById(R.id.checkBoxWindows);
        checkBoxLinux = view.findViewById(R.id.checkBoxLinux);
        checkBoxMacOS = view.findViewById(R.id.checkBoxMacOS);
        
        // Инициализация радиогруппы для частоты обновлений
        radioGroupUpdateFreq = view.findViewById(R.id.radioGroupUpdateFreq);
        
        buttonFinishStep4 = view.findViewById(R.id.buttonFinishStep4);
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка выбора хотя бы одной цели
        if (!checkBoxBasicNeeds.isChecked() && 
            !checkBoxProblemSolving.isChecked() && 
            !checkBoxQualityImprovement.isChecked() &&
            !checkBoxPersonalExpression.isChecked() &&
            !checkBoxResourceSaving.isChecked() &&
            !checkBoxPleasure.isChecked() &&
            !checkBoxSafety.isChecked()) {
            Toast.makeText(requireContext(), "Выберите хотя бы одну цель или задачу", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка выбора хотя бы одного мотива
        if (!checkBoxNeedsMotives.isChecked() && 
            !checkBoxPersonalMotives.isChecked() && 
            !checkBoxEconomicMotives.isChecked() &&
            !checkBoxSocialMotives.isChecked() &&
            !checkBoxPracticalMotives.isChecked() &&
            !checkBoxPsychologicalMotives.isChecked()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один мотив", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка заполнения соотношения целей с продуктами
        if (editTextGoalsRelation.getText().toString().trim().isEmpty()) {
            editTextGoalsRelation.setError("Пожалуйста, опишите связь между целями ЦА и вашими продуктами/услугами");
            isValid = false;
        }
        
        // Проверка выбора отношения к технологиям
        if (radioGroupTechAttitude.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Выберите отношение к технологиям", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Проверка выбора использования современных технологий
        if (radioGroupUseTech.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Укажите, используют ли современные технологии", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Собираем данные о целях и задачах
        StringBuilder goals = new StringBuilder();
        if (checkBoxBasicNeeds.isChecked()) goals.append("Удовлетворение потребностей, ");
        if (checkBoxProblemSolving.isChecked()) goals.append("Решение проблемы или задачи, ");
        if (checkBoxQualityImprovement.isChecked()) goals.append("Улучшение качества жизни, ");
        if (checkBoxPersonalExpression.isChecked()) goals.append("Выражение личности и стиля жизни, ");
        if (checkBoxResourceSaving.isChecked()) goals.append("Экономия времени и денег, ");
        if (checkBoxPleasure.isChecked()) goals.append("Получение удовольствия и удовлетворения, ");
        if (checkBoxSafety.isChecked()) goals.append("Безопасность и уверенность в выборе, ");
        
        // Собираем данные о мотивах
        StringBuilder motives = new StringBuilder();
        if (checkBoxNeedsMotives.isChecked()) motives.append("Потребностные мотивы, ");
        if (checkBoxPersonalMotives.isChecked()) motives.append("Личностные мотивы, ");
        if (checkBoxEconomicMotives.isChecked()) motives.append("Экономические мотивы, ");
        if (checkBoxSocialMotives.isChecked()) motives.append("Социальные мотивы, ");
        if (checkBoxPracticalMotives.isChecked()) motives.append("Практические мотивы, ");
        if (checkBoxPsychologicalMotives.isChecked()) motives.append("Психологические мотивы, ");
        
        // Собираем данные о барьерах при покупке
        StringBuilder barriers = new StringBuilder();
        if (checkBoxFinancialBarriers.isChecked()) barriers.append("Финансовые ограничения, ");
        if (checkBoxInfoBarriers.isChecked()) barriers.append("Недостаток информации, ");
        if (checkBoxPriceBarriers.isChecked()) barriers.append("Неясность в цене и условиях, ");
        if (checkBoxTrustBarriers.isChecked()) barriers.append("Недоверие к бренду или поставщику, ");
        if (checkBoxExperienceBarriers.isChecked()) barriers.append("Отсутствие демонстрации или опыта использования, ");
        if (checkBoxTimeBarriers.isChecked()) barriers.append("Ограничения времени или удобства, ");
        if (checkBoxSocialBarriers.isChecked()) barriers.append("Социальные или культурные факторы, ");
        if (checkBoxNeedsBarriers.isChecked()) barriers.append("Отсутствие удовлетворения потребностей, ");
        
        // Собираем данные об устройствах и платформах
        StringBuilder platforms = new StringBuilder();
        if (checkBoxAndroid.isChecked()) platforms.append("Android, ");
        if (checkBoxIOS.isChecked()) platforms.append("iOS, ");
        if (checkBoxWindows.isChecked()) platforms.append("Windows, ");
        if (checkBoxLinux.isChecked()) platforms.append("Linux, ");
        if (checkBoxMacOS.isChecked()) platforms.append("macOS, ");
        
        // Получаем данные из текстовых полей
        String goalsRelation = editTextGoalsRelation.getText().toString().trim();
        String productChoice = editTextProductChoice.getText().toString().trim();
        String usageBarriers = editTextUsageBarriers.getText().toString().trim();
        
        // Получаем выбранное отношение к технологиям
        String techAttitude = "";
        int techAttitudeId = radioGroupTechAttitude.getCheckedRadioButtonId();
        if (techAttitudeId == R.id.radioPositive) {
            techAttitude = "Положительное отношение";
        } else if (techAttitudeId == R.id.radioNeutral) {
            techAttitude = "Нейтральное отношение";
        } else if (techAttitudeId == R.id.radioNegative) {
            techAttitude = "Отрицательное отношение";
        } else if (techAttitudeId == R.id.radioAmbivalent) {
            techAttitude = "Амбивалентное отношение";
        }
        
        // Получаем данные об использовании современных технологий
        String useTech = (radioGroupUseTech.getCheckedRadioButtonId() == R.id.radioYes) ? "Да" : "Нет";
        
        // Получаем данные о частоте обновлений
        String updateFreq = "";
        int updateFreqId = radioGroupUpdateFreq.getCheckedRadioButtonId();
        if (updateFreqId == R.id.radioMonthly) {
            updateFreq = "Обновляют ежемесячно";
        } else if (updateFreqId == R.id.radioQuarterly) {
            updateFreq = "Обновляются раз в квартал";
        } else if (updateFreqId == R.id.radio2_3Years) {
            updateFreq = "Обновляют устройства раз в 2-3 года";
        } else if (updateFreqId == R.id.radioLatest) {
            updateFreq = "Используют последние версии ПО ежедневно";
        } else if (updateFreqId == R.id.radioRare) {
            updateFreq = "Обновляют редко";
        }
        
        // В реальном приложении здесь сохраняем данные в ViewModel или БД
        
        // Сохраняем в SharedPreferences что шаг выполнен
        requireContext().getSharedPreferences("steps_completion", 0)
            .edit()
            .putBoolean("step4_completed", true)
            .apply();
    }
} 