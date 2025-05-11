package com.example.ultai.planer.stages.stage_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;

public class Step6Fragment extends Fragment {

    private SeekBar seekBarLoyalty;
    private TextView textViewLoyaltyValue;
    private SeekBar seekBarCommunityInvolvement;
    private TextView textViewCommunityInvolvementValue;
    private SeekBar seekBarMarketingAttitude;
    private TextView textViewMarketingAttitudeValue;
    private RadioGroup radioGroupCommunicationFrequency;
    private Button buttonFinishStep6;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step6, container, false);

        // Инициализация компонентов UI
        initializeUI(view);

        // Настройка SeekBar для уровня лояльности
        setupSeekBars();

        // Настройка кнопки завершения шага
        buttonFinishStep6.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Особенности взаимодействия с брендом сохранены!", Toast.LENGTH_SHORT).show();
                
                // Переходим к этапу 2
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step6Fragment_to_faza2_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        seekBarLoyalty = view.findViewById(R.id.seekBarLoyalty);
        textViewLoyaltyValue = view.findViewById(R.id.textViewLoyaltyValue);
        seekBarCommunityInvolvement = view.findViewById(R.id.seekBarCommunityInvolvement);
        textViewCommunityInvolvementValue = view.findViewById(R.id.textViewCommunityInvolvementValue);
        seekBarMarketingAttitude = view.findViewById(R.id.seekBarMarketingAttitude);
        textViewMarketingAttitudeValue = view.findViewById(R.id.textViewMarketingAttitudeValue);
        radioGroupCommunicationFrequency = view.findViewById(R.id.radioGroupCommunicationFrequency);
        buttonFinishStep6 = view.findViewById(R.id.buttonFinishStep6);
    }

    /**
     * Настройка SeekBar для всех шкал оценки
     */
    private void setupSeekBars() {
        // Настраиваем шкалу лояльности к бренду
        seekBarLoyalty.setProgress(3);
        updateTextValue(textViewLoyaltyValue, 3);
        
        seekBarLoyalty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextValue(textViewLoyaltyValue, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Настраиваем шкалу вовлеченности в сообщество бренда
        seekBarCommunityInvolvement.setProgress(3);
        updateTextValue(textViewCommunityInvolvementValue, 3);
        
        seekBarCommunityInvolvement.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextValue(textViewCommunityInvolvementValue, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Настраиваем шкалу отношения к рекламе и маркетингу
        seekBarMarketingAttitude.setProgress(3);
        updateTextValue(textViewMarketingAttitudeValue, 3);
        
        seekBarMarketingAttitude.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextValue(textViewMarketingAttitudeValue, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    /**
     * Обновить текстовое представление значения на шкале
     */
    private void updateTextValue(TextView textView, int value) {
        textView.setText(String.valueOf(value));
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка выбора частоты коммуникаций от бренда
        if (radioGroupCommunicationFrequency.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Выберите частоту получения коммуникаций от бренда", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Получаем уровень лояльности к бренду
        int loyaltyLevel = seekBarLoyalty.getProgress();
        
        // Получаем уровень вовлеченности в сообщество бренда
        int communityInvolvementLevel = seekBarCommunityInvolvement.getProgress();
        
        // Получаем уровень отношения к рекламе и маркетингу
        int marketingAttitudeLevel = seekBarMarketingAttitude.getProgress();
        
        // Получаем выбранную частоту коммуникаций от бренда
        String communicationFrequency = "";
        int selectedId = radioGroupCommunicationFrequency.getCheckedRadioButtonId();
        
        if (selectedId != -1) {
            RadioButton selectedRadioButton = getView().findViewById(selectedId);
            communicationFrequency = selectedRadioButton.getText().toString();
            
            // Для краткости можно хранить только первое слово
            if (communicationFrequency.contains(":")) {
                communicationFrequency = communicationFrequency.substring(0, communicationFrequency.indexOf(":"));
            }
        }
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("steps_completion", Context.MODE_PRIVATE);
        sharedPreferences.edit()
            .putBoolean("step6_completed", true)
            .apply();
            
        // В реальном приложении также сохраняем значения в БД или ViewModel
        // Например:
        // SharedPreferences dataPref = requireContext().getSharedPreferences("brand_interaction_data", Context.MODE_PRIVATE);
        // dataPref.edit()
        //     .putInt("loyalty_level", loyaltyLevel)
        //     .putInt("community_involvement_level", communityInvolvementLevel)
        //     .putInt("marketing_attitude_level", marketingAttitudeLevel)
        //     .putString("communication_frequency", communicationFrequency)
        //     .apply();
    }
} 