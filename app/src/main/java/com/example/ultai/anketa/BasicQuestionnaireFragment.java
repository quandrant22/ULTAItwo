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
        
        binding.backButton.setOnClickListener(v -> {
            if (navController.popBackStack() == false) {
                 if (getActivity() != null) {
                    getActivity().onBackPressed();
                 }
            }
        });
        
        initializeSpinners();
        setupRadioButtons();
        
        binding.finishButton.setOnClickListener(v -> {
            if (validateForm()) {
                collectAndSaveQuestionnaireData();
            }
        });
    }
    
    private void setupRadioButtons() {
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
                    Toast.makeText(getContext(), getString(R.string.message_questionnaire_saved), Toast.LENGTH_SHORT).show();
                    if (navController != null) {
                        navController.navigate(R.id.action_basicQuestionnaireFragment_to_navigation_home);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getContext(), getString(R.string.error_saving_questionnaire, errorMessage), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 