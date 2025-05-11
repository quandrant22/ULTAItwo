package com.example.ultai.anketa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;
import com.example.ultai.databinding.FragmentBasicQuestionnaireBinding;
import com.example.ultai.models.BasicQuestionnaire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicQuestionnaireFragment extends Fragment {

    private FragmentBasicQuestionnaireBinding binding;
    private BasicQuestionnaireViewModel viewModel;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBasicQuestionnaireBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Инициализация ViewModel и NavController
        viewModel = new ViewModelProvider(requireActivity()).get(BasicQuestionnaireViewModel.class);
        navController = Navigation.findNavController(view);
        
        // Настройка кнопки "Назад"
        binding.backButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        
        // Инициализация спиннеров
        initializeSpinners();
        
        // Настройка радиокнопок
        setupRadioButtons();
        
        // Настройка кнопки "Завершить"
        binding.finishButton.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                navController.navigate(R.id.action_basicQuestionnaireFragment_to_navigation_home);
                Toast.makeText(requireContext(), getString(R.string.message_questionnaire_saved), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupRadioButtons() {
        // Настройка радиогруппы состояния бизнеса
        binding.planningRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.launchedRadioButton.setChecked(false);
            }
        });
        
        binding.launchedRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.planningRadioButton.setChecked(false);
            }
        });
        
        // Настройка радиогруппы типа деятельности
        binding.goodsRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.servicesRadioButton.setChecked(false);
            }
        });
        
        binding.servicesRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.goodsRadioButton.setChecked(false);
            }
        });
    }

    private void initializeSpinners() {
        // Список стран
        List<String> countries = new ArrayList<>(Arrays.asList("Выберите страну", "Россия", "Казахстан", "Беларусь", "Украина"));
        
        // Адаптер для спиннера стран
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_spinner_item,
                countries
        );
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.countrySpinner.setAdapter(countryAdapter);
        
        // Адаптер для спиннера страны реализации (такой же список)
        ArrayAdapter<String> implementationCountryAdapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_spinner_item,
                countries
        );
        implementationCountryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.countryImplementationSpinner.setAdapter(implementationCountryAdapter);
        
        // Список городов (пример)
        List<String> cities = new ArrayList<>(Arrays.asList("Выберите город", "Москва", "Санкт-Петербург", "Казань", "Новосибирск"));
        
        // Адаптер для спиннера городов
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_spinner_item,
                cities
        );
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.citySpinner.setAdapter(cityAdapter);
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка названия компании
        if (binding.companyNameEditText.getText().toString().trim().isEmpty()) {
            binding.companyNameEditText.setError("Введите название компании");
            isValid = false;
        }
        
        // Проверка выбора состояния бизнеса
        if (!binding.planningRadioButton.isChecked() && !binding.launchedRadioButton.isChecked()) {
            Toast.makeText(requireContext(), "Выберите текущее состояние бизнеса", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка выбора страны
        if (binding.countrySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Выберите страну", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка выбора типа деятельности
        if (!binding.goodsRadioButton.isChecked() && !binding.servicesRadioButton.isChecked()) {
            Toast.makeText(requireContext(), "Выберите тип деятельности", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка описания услуг/товаров
        if (binding.servicesDescriptionEditText.getText().toString().trim().isEmpty()) {
            binding.servicesDescriptionEditText.setError("Укажите перечень услуг/товаров");
            isValid = false;
        }
        
        // Проверка выбора страны реализации
        if (binding.countryImplementationSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Выберите страну реализации", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка выбора города
        if (binding.citySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Выберите город", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }
    
    private void saveFormData() {
        // Сохранение данных формы в ViewModel
        BasicQuestionnaire questionnaire = new BasicQuestionnaire();
        
        questionnaire.setCompanyName(binding.companyNameEditText.getText().toString().trim());
        
        if (binding.planningRadioButton.isChecked()) {
            questionnaire.setBusinessState("Планирую запустить");
        } else if (binding.launchedRadioButton.isChecked()) {
            questionnaire.setBusinessState("Уже запущен");
        }
        
        questionnaire.setCountry(binding.countrySpinner.getSelectedItem().toString());
        
        if (binding.goodsRadioButton.isChecked()) {
            questionnaire.setActivityType("Товары");
        } else if (binding.servicesRadioButton.isChecked()) {
            questionnaire.setActivityType("Услуги");
        }
        
        questionnaire.setProductsServicesDescription(binding.servicesDescriptionEditText.getText().toString().trim());
        questionnaire.setMarketScope(binding.countryImplementationSpinner.getSelectedItem().toString());
        questionnaire.setCity(binding.citySpinner.getSelectedItem().toString());
        
        // Сохранение данных в ViewModel
        viewModel.setQuestionnaire(questionnaire);
        
        // Сохранение данных через API или в локальное хранилище
        viewModel.saveQuestionnaireData(1, new BasicQuestionnaireViewModel.OnQuestionnaireSaveListener() {
            @Override
            public void onSuccess() {
                // Данные успешно сохранены
            }

            @Override
            public void onError(String errorMessage) {
                // Ошибка при сохранении данных
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), 
                            getString(R.string.error_saving_questionnaire, errorMessage), 
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 