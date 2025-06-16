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

import com.example.ultai20.R;
import com.example.ultai20.databinding.FragmentBasicQuestionnaireBinding;
import com.example.ultai.models.BasicQuestionnaire;
import com.example.ultai.data.repository.UserRepository;

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
        
        viewModel = new ViewModelProvider(requireActivity()).get(BasicQuestionnaireViewModel.class);
        navController = Navigation.findNavController(view);
        
        // Показываем приветственное сообщение для новых пользователей
        Toast.makeText(getContext(), "Добро пожаловать! Пожалуйста, заполните базовую анкету для настройки приложения", Toast.LENGTH_LONG).show();
        
        binding.backButton.setOnClickListener(v -> {
            // При нажатии назад из анкеты нового пользователя возвращаемся к экрану входа
            if (navController.popBackStack() == false) {
                 if (getActivity() != null) {
                    getActivity().onBackPressed();
                 }
            }
        });
        
        initializeSpinners();
        
        binding.finishButton.setOnClickListener(v -> {
            if (validateForm()) {
                // Показываем индикатор загрузки и прогресс
                binding.finishButton.setEnabled(false);
                binding.finishButton.setText("Сохранение...");
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.setProgress(100);
                collectAndSaveQuestionnaireData();
            }
        });
        
        // Настраиваем обновление прогресса при заполнении полей
        setupProgressTracking();
    }
    
    private void initializeSpinners() {
        List<String> countries = new ArrayList<>(Arrays.asList("Выберите страну", "Россия", "Казахстан", "Беларусь", "Украина"));
        
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_spinner_item,
                countries
        );
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.countrySpinner.setAdapter(countryAdapter);
        
        ArrayAdapter<String> implementationCountryAdapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_spinner_item,
                countries
        );
        implementationCountryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.countryImplementationSpinner.setAdapter(implementationCountryAdapter);
        
        List<String> cities = new ArrayList<>(Arrays.asList("Выберите город", "Москва", "Санкт-Петербург", "Казань", "Новосибирск"));
        
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
        
        if (binding.companyNameEditText.getText().toString().trim().isEmpty()) {
            binding.companyNameEditText.setError("Введите название компании");
            isValid = false;
        }
        
        if (!binding.planningRadioButton.isChecked() && !binding.launchedRadioButton.isChecked()) {
            Toast.makeText(requireContext(), "Выберите текущее состояние бизнеса", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        if (binding.countrySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Выберите страну", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        if (!binding.goodsRadioButton.isChecked() && !binding.servicesRadioButton.isChecked()) {
            Toast.makeText(requireContext(), "Выберите тип деятельности", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        if (binding.servicesDescriptionEditText.getText().toString().trim().isEmpty()) {
            binding.servicesDescriptionEditText.setError("Укажите перечень услуг/товаров");
            isValid = false;
        }
        
        if (binding.countryImplementationSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Выберите страну реализации", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        if (binding.citySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Выберите город", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }
    
    private void collectAndSaveQuestionnaireData() {
        BasicQuestionnaire questionnaire = new BasicQuestionnaire();
        
        questionnaire.setCompanyName(binding.companyNameEditText.getText().toString().trim());
        
        if (binding.planningRadioButton.isChecked()) {
            questionnaire.setBusinessState("Планирую запустить");
        } else if (binding.launchedRadioButton.isChecked()) {
            questionnaire.setBusinessState("Уже запущен");
        }
        
        if (binding.countrySpinner.getSelectedItem() != null) {
            questionnaire.setCountry(binding.countrySpinner.getSelectedItem().toString());
        }
        
        if (binding.goodsRadioButton.isChecked()) {
            questionnaire.setActivityType("Товары");
        } else if (binding.servicesRadioButton.isChecked()) {
            questionnaire.setActivityType("Услуги");
        }
        
        questionnaire.setProductsServicesDescription(binding.servicesDescriptionEditText.getText().toString().trim());
        
        if (binding.countryImplementationSpinner.getSelectedItem() != null) {
            questionnaire.setMarketScope(binding.countryImplementationSpinner.getSelectedItem().toString()); 
        }
        if (binding.citySpinner.getSelectedItem() != null) {
            questionnaire.setCity(binding.citySpinner.getSelectedItem().toString());
        }
        
        viewModel.setQuestionnaire(questionnaire);

        viewModel.saveQuestionnaireDataToFirebase(new BasicQuestionnaireViewModel.OnQuestionnaireSaveListener() {
            @Override
            public void onSuccess() {
                if (isAdded() && getActivity() != null) {
                    // Восстанавливаем состояние кнопки
                    binding.finishButton.setEnabled(true);
                    binding.finishButton.setText("Завершить");
                    
                    Toast.makeText(getContext(), "Анкета успешно сохранена! Добро пожаловать в ULTAI", Toast.LENGTH_LONG).show();
                    
                    // Переходим на главный экран после успешного сохранения
                    if (navController != null) {
                        navController.navigate(R.id.action_basicQuestionnaireFragment_to_navigation_home);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded() && getActivity() != null) {
                    // Восстанавливаем состояние кнопки
                    binding.finishButton.setEnabled(true);
                    binding.finishButton.setText("Завершить");
                    
                    Toast.makeText(getContext(), "Ошибка сохранения анкеты: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupProgressTracking() {
        // Массив для отслеживания заполненных полей
        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                updateProgress();
            }
        };
        
        binding.companyNameEditText.addTextChangedListener(textWatcher);
        binding.servicesDescriptionEditText.addTextChangedListener(textWatcher);
        
        android.widget.AdapterView.OnItemSelectedListener spinnerListener = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateProgress();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        };
        
        binding.countrySpinner.setOnItemSelectedListener(spinnerListener);
        binding.countryImplementationSpinner.setOnItemSelectedListener(spinnerListener);
        binding.citySpinner.setOnItemSelectedListener(spinnerListener);
        
        binding.planningRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.launchedRadioButton.setChecked(false);
                updateProgress();
            }
        });
        
        binding.launchedRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.planningRadioButton.setChecked(false);
                updateProgress();
            }
        });
        
        binding.goodsRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.servicesRadioButton.setChecked(false);
                updateProgress();
            }
        });
        
        binding.servicesRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.goodsRadioButton.setChecked(false);
                updateProgress();
            }
        });
        
        // Изначально показываем прогресс-бар
        binding.progressBar.setVisibility(View.VISIBLE);
        updateProgress();
    }
    
    private void updateProgress() {
        int filledFields = 0;
        int totalFields = 6; // Общее количество полей для заполнения
        
        // Проверяем название компании
        if (!binding.companyNameEditText.getText().toString().trim().isEmpty()) {
            filledFields++;
        }
        
        // Проверяем состояние бизнеса
        if (binding.planningRadioButton.isChecked() || binding.launchedRadioButton.isChecked()) {
            filledFields++;
        }
        
        // Проверяем страну
        if (binding.countrySpinner.getSelectedItemPosition() > 0) {
            filledFields++;
        }
        
        // Проверяем тип деятельности
        if (binding.goodsRadioButton.isChecked() || binding.servicesRadioButton.isChecked()) {
            filledFields++;
        }
        
        // Проверяем описание услуг
        if (!binding.servicesDescriptionEditText.getText().toString().trim().isEmpty()) {
            filledFields++;
        }
        
        // Проверяем географию (страна + город)
        if (binding.countryImplementationSpinner.getSelectedItemPosition() > 0 && 
            binding.citySpinner.getSelectedItemPosition() > 0) {
            filledFields++;
        }
        
        int progress = (filledFields * 100) / totalFields;
        binding.progressBar.setProgress(progress);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 