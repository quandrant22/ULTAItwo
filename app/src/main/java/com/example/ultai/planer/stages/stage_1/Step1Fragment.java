package com.example.ultai.planer.stages.stage_1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Step1Fragment extends Fragment {

    private RadioGroup radioGroupAge;
    private RadioGroup radioGroupFamily;
    private RadioGroup radioGroupChildren;
    private Spinner spinnerCity;
    private Spinner spinnerRegion;
    private Button buttonFinishStep1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step1, container, false);

        // Инициализация компонентов UI
        radioGroupAge = view.findViewById(R.id.radioGroupAge);
        radioGroupFamily = view.findViewById(R.id.radioGroupFamily);
        radioGroupChildren = view.findViewById(R.id.radioGroupChildren);
        spinnerCity = view.findViewById(R.id.spinnerCity);
        spinnerRegion = view.findViewById(R.id.spinnerRegion);
        buttonFinishStep1 = view.findViewById(R.id.buttonFinishStep1);

        // Настройка спиннеров
        setupSpinners();

        // Настройка кнопки завершения шага
        buttonFinishStep1.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
            }
        });

        return view;
    }

    /**
     * Настройка спиннеров для городов и регионов
     */
    private void setupSpinners() {
        // Пример городов
        List<String> cities = new ArrayList<>(Arrays.asList(
                "Выберите город", "Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург", 
                "Казань", "Нижний Новгород", "Челябинск", "Самара", "Омск", "Ростов-на-Дону"));
        
        // Пример регионов
        List<String> regions = new ArrayList<>(Arrays.asList(
                "Выберите регион", "Московская область", "Ленинградская область", "Свердловская область",
                "Новосибирская область", "Республика Татарстан", "Нижегородская область"));

        // Создаем адаптеры для спиннеров
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);

        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, regions);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRegion.setAdapter(regionAdapter);
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;

        // Проверка выбора возраста
        if (radioGroupAge.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Выберите возраст целевой аудитории", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Проверка выбора семейного положения
        if (radioGroupFamily.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Выберите семейное положение целевой аудитории", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Проверка выбора наличия детей
        if (radioGroupChildren.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Выберите количество детей целевой аудитории", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Проверка выбора города
        if (spinnerCity.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Выберите город", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем выбранный возраст
        RadioButton selectedAgeButton = getView().findViewById(radioGroupAge.getCheckedRadioButtonId());
        String selectedAge = selectedAgeButton != null ? selectedAgeButton.getText().toString() : "";

        // Получаем выбранное семейное положение
        RadioButton selectedFamilyButton = getView().findViewById(radioGroupFamily.getCheckedRadioButtonId());
        String selectedFamily = selectedFamilyButton != null ? selectedFamilyButton.getText().toString() : "";

        // Получаем выбранное количество детей
        RadioButton selectedChildrenButton = getView().findViewById(radioGroupChildren.getCheckedRadioButtonId());
        String selectedChildren = selectedChildrenButton != null ? selectedChildrenButton.getText().toString() : "";

        // Получаем выбранный город и регион
        String selectedCity = spinnerCity.getSelectedItem().toString();
        String selectedRegion = spinnerRegion.getSelectedItem().toString();

        // В реальном приложении здесь сохраняем данные в ViewModel или БД
        // Например:
        // StepViewModel viewModel = new ViewModelProvider(requireActivity()).get(StepViewModel.class);
        // viewModel.saveStep1Data(selectedAge, selectedFamily, selectedChildren, selectedCity, selectedRegion);
        
        // Сохраняем в SharedPreferences что шаг выполнен
        requireContext().getSharedPreferences("steps_completion", 0)
            .edit()
            .putBoolean("step1_completed", true)
            .apply();
        
        // Показываем уведомление об успешном завершении
        Toast.makeText(requireContext(), "Демографические характеристики сохранены!", Toast.LENGTH_SHORT).show();
        
        // Возвращаемся к списку этапов
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_step1Fragment_to_faza1_stages);
    }

    /**
     * Интерфейс для связи с родительским фрагментом
     */
    public interface StepCompletionListener {
        void onStepCompleted(int stepNumber);
    }
} 