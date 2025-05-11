package com.example.ultai.planer.stages.stage_1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;

public class Stage_1Fragment extends Fragment implements Step1Fragment.StepCompletionListener {

    // Переменные для хранения контейнеров шагов
    private ConstraintLayout step1Container;
    private ConstraintLayout step2Container;
    private ConstraintLayout step3Container;
    private ConstraintLayout step4Container;
    private ConstraintLayout step5Container;
    private ConstraintLayout step6Container;
    
    // Массив для отслеживания завершенных шагов
    private boolean[] completedSteps = new boolean[6];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_stage_1, container, false);

        // Настройка слушателей для ImageView в горизонтальном скролле
        setupImageClickListeners(view);

        // Инициализация и настройка интерфейса для шагов
        initializeStepsUI(view);

        return view;
    }

    /**
     * Инициализация UI элементов для шагов этапа 1
     */
    private void initializeStepsUI(View view) {
        // Получаем ссылки на контейнеры шагов
        step1Container = view.findViewById(R.id.step_1_container);
        step2Container = view.findViewById(R.id.step_2_container);
        step3Container = view.findViewById(R.id.step_3_container);
        step4Container = view.findViewById(R.id.step_4_container);
        step5Container = view.findViewById(R.id.step_5_container);
        step6Container = view.findViewById(R.id.step_6_container);

        // Настраиваем обработчики событий для шагов
        setupStepClickListeners();
        
        // Инициализация текстов шагов
        setupStepTexts();
    }
    
    /**
     * Настройка текстов для шагов на основе файла с описанием шагов этапа 1 фазы 1
     */
    private void setupStepTexts() {
        // Находим TextView и устанавливаем заголовки шагов
        if (step1Container.findViewById(R.id.step_1_title) != null) {
            androidx.appcompat.widget.AppCompatTextView step1Title = step1Container.findViewById(R.id.step_1_title);
            androidx.appcompat.widget.AppCompatTextView step1Desc = step1Container.findViewById(R.id.step_1_desc);
            step1Title.setText("Шаг 1");
            step1Desc.setText("ДЕМОГРАФИЧЕСКИЕ ХАРАКТЕРИСТИКИ");
        }
        
        if (step2Container.findViewById(R.id.step_2_title) != null) {
            androidx.appcompat.widget.AppCompatTextView step2Title = step2Container.findViewById(R.id.step_2_title);
            androidx.appcompat.widget.AppCompatTextView step2Desc = step2Container.findViewById(R.id.step_2_desc);
            step2Title.setText("Шаг 2");
            step2Desc.setText("СОЦИАЛЬНЫЕ ХАРАКТЕРИСТИКИ");
        }
        
        if (step3Container.findViewById(R.id.step_3_title) != null) {
            androidx.appcompat.widget.AppCompatTextView step3Title = step3Container.findViewById(R.id.step_3_title);
            androidx.appcompat.widget.AppCompatTextView step3Desc = step3Container.findViewById(R.id.step_3_desc);
            step3Title.setText("Шаг 3");
            step3Desc.setText("ПСИХОЛОГИЧЕСКИЕ ХАРАКТЕРИСТИКИ");
        }
        
        if (step4Container.findViewById(R.id.step_4_title) != null) {
            androidx.appcompat.widget.AppCompatTextView step4Title = step4Container.findViewById(R.id.step_4_title);
            androidx.appcompat.widget.AppCompatTextView step4Desc = step4Container.findViewById(R.id.step_4_desc);
            step4Title.setText("Шаг 4");
            step4Desc.setText("ПСИХОГРАФИЧЕСКИЕ ХАРАКТЕРИСТИКИ");
        }
        
        if (step5Container.findViewById(R.id.step_5_title) != null) {
            androidx.appcompat.widget.AppCompatTextView step5Title = step5Container.findViewById(R.id.step_5_title);
            androidx.appcompat.widget.AppCompatTextView step5Desc = step5Container.findViewById(R.id.step_5_desc);
            step5Title.setText("Шаг 5");
            step5Desc.setText("ПОВЕДЕНЧЕСКИЕ ХАРАКТЕРИСТИКИ");
        }
        
        if (step6Container.findViewById(R.id.step_6_title) != null) {
            androidx.appcompat.widget.AppCompatTextView step6Title = step6Container.findViewById(R.id.step_6_title);
            androidx.appcompat.widget.AppCompatTextView step6Desc = step6Container.findViewById(R.id.step_6_desc);
            step6Title.setText("Шаг 6");
            step6Desc.setText("ОСОБЕННОСТИ ВЗАИМОДЕЙСТВИЯ С БРЕНДОМ");
        }
    }

    /**
     * Настройка обработчиков нажатий для шагов
     */
    private void setupStepClickListeners() {
        // Шаг 1 - открытие анкеты демографических характеристик
        step1Container.setOnClickListener(v -> {
            navigateToStep(R.id.action_faza1_stages_to_step1Fragment);
        });

        // Шаг 2 - открытие анкеты социальных характеристик (если Шаг 1 завершен)
        step2Container.setOnClickListener(v -> {
            if (completedSteps[0]) {
                navigateToStep(R.id.action_faza1_stages_to_step2Fragment);
            } else {
                Toast.makeText(requireContext(), "Сначала выполните Шаг 1", Toast.LENGTH_SHORT).show();
            }
        });

        // Шаг 3 - психологические характеристики
        step3Container.setOnClickListener(v -> {
            if (completedSteps[0] && completedSteps[1]) {
                navigateToStep(R.id.action_faza1_stages_to_step3Fragment);
            } else {
                Toast.makeText(requireContext(), "Сначала выполните Шаги 1 и 2", Toast.LENGTH_SHORT).show();
            }
        });

        // Шаг 4 - психографические характеристики
        step4Container.setOnClickListener(v -> {
            if (completedSteps[0] && completedSteps[1] && completedSteps[2]) {
                navigateToStep(R.id.action_faza1_stages_to_step4Fragment);
            } else {
                Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
            }
        });

        // Шаг 5 - поведенческие характеристики
        step5Container.setOnClickListener(v -> {
            if (completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3]) {
                navigateToStep(R.id.action_faza1_stages_to_step5Fragment);
            } else {
                Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
            }
        });

        // Шаг 6 - особенности взаимодействия с брендом
        step6Container.setOnClickListener(v -> {
            if (completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3] && completedSteps[4]) {
                navigateToStep(R.id.action_faza1_stages_to_step6Fragment);
            } else {
                Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Навигация к нужному шагу с использованием NavController
     */
    private void navigateToStep(int actionId) {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(actionId);
    }

    /**
     * Обновить отображение шагов на основе их состояния (выполнен/не выполнен)
     */
    private void updateStepsUI() {
        // Обновляем UI шага 2 (активируем, если шаг 1 выполнен)
        if (completedSteps[0]) {
            step2Container.setBackgroundResource(R.drawable.rounded_rectangle_step);
            
            // Обновляем текст и цвет текста
            androidx.appcompat.widget.AppCompatTextView titleTextView = step2Container.findViewById(R.id.step_2_title);
            androidx.appcompat.widget.AppCompatTextView descTextView = step2Container.findViewById(R.id.step_2_desc);
            
            if (titleTextView != null) {
                titleTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
            
            if (descTextView != null) {
                descTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
        }
        
        // Обновляем UI шага 3 (активируем, если шаг 2 выполнен)
        if (completedSteps[1]) {
            step3Container.setBackgroundResource(R.drawable.rounded_rectangle_step);
            
            // Обновляем текст и цвет текста
            androidx.appcompat.widget.AppCompatTextView titleTextView = step3Container.findViewById(R.id.step_3_title);
            androidx.appcompat.widget.AppCompatTextView descTextView = step3Container.findViewById(R.id.step_3_desc);
            
            if (titleTextView != null) {
                titleTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
            
            if (descTextView != null) {
                descTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
        }
        
        // Обновляем UI шага 4 (активируем, если шаг 3 выполнен)
        if (completedSteps[2]) {
            step4Container.setBackgroundResource(R.drawable.rounded_rectangle_step);
            
            // Обновляем текст и цвет текста
            androidx.appcompat.widget.AppCompatTextView titleTextView = step4Container.findViewById(R.id.step_4_title);
            androidx.appcompat.widget.AppCompatTextView descTextView = step4Container.findViewById(R.id.step_4_desc);
            
            if (titleTextView != null) {
                titleTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
            
            if (descTextView != null) {
                descTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
        }
        
        // Обновляем UI шага 5 (активируем, если шаг 4 выполнен)
        if (completedSteps[3]) {
            step5Container.setBackgroundResource(R.drawable.rounded_rectangle_step);
            
            // Обновляем текст и цвет текста
            androidx.appcompat.widget.AppCompatTextView titleTextView = step5Container.findViewById(R.id.step_5_title);
            androidx.appcompat.widget.AppCompatTextView descTextView = step5Container.findViewById(R.id.step_5_desc);
            
            if (titleTextView != null) {
                titleTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
            
            if (descTextView != null) {
                descTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
        }
        
        // Обновляем UI шага 6 (активируем, если шаг 5 выполнен)
        if (completedSteps[4]) {
            step6Container.setBackgroundResource(R.drawable.rounded_rectangle_step);
            
            // Обновляем текст и цвет текста
            androidx.appcompat.widget.AppCompatTextView titleTextView = step6Container.findViewById(R.id.step_6_title);
            androidx.appcompat.widget.AppCompatTextView descTextView = step6Container.findViewById(R.id.step_6_desc);
            
            if (titleTextView != null) {
                titleTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
            
            if (descTextView != null) {
                descTextView.setTextColor(getResources().getColor(R.color.dark_gray));
            }
        }
    }

    /**
     * Настройка слушателей для ImageView в горизонтальном скролле
     */
    private void setupImageClickListeners(View view) {
        // Этап 1
        view.findViewById(R.id.stage_1).setOnClickListener(v -> {
            // При клике на Этап 1 не нужно ничего делать, так как уже находимся в нем
        });

        // Этап 2
        view.findViewById(R.id.stage_2).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Сначала завершите Этап 1", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Находим кнопку и настраиваем переход на другой фрагмент
        ImageButton imageButtonNext = view.findViewById(R.id.imageButton3);
        imageButtonNext.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_faza1_stages_to_navigation_planer);
        });
        
        ImageButton imageButtonSettings = view.findViewById(R.id.imageButton7);
        imageButtonSettings.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_faza1_stages_to_profileFragment);
        });
        
        // Проверяем статус выполнения шагов из SharedPreferences
        checkStepsCompletionStatus();
    }
    
    /**
     * Проверяет статус выполнения всех шагов из SharedPreferences
     */
    private void checkStepsCompletionStatus() {
        // Получаем SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("steps_completion", 0);
        
        // Проверяем все шаги по порядку
        completedSteps[0] = prefs.getBoolean("step1_completed", false);
        completedSteps[1] = prefs.getBoolean("step2_completed", false);
        completedSteps[2] = prefs.getBoolean("step3_completed", false);
        completedSteps[3] = prefs.getBoolean("step4_completed", false);
        completedSteps[4] = prefs.getBoolean("step5_completed", false);
        completedSteps[5] = prefs.getBoolean("step6_completed", false);
        
        // Обновляем UI в соответствии с выполненными шагами
        updateStepsUI();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Обновляем статус выполнения шагов при возвращении на фрагмент
        checkStepsCompletionStatus();
    }

    // Реализация интерфейса StepCompletionListener
    @Override
    public void onStepCompleted(int stepNumber) {
        Log.d("Stage_1Fragment", "Шаг " + stepNumber + " завершен.");
        // Здесь можно добавить логику обновления UI или сохранения состояния
        // Например, отметить шаг как пройденный
        updateStepUI(stepNumber, true);
    }

    // Метод для обновления UI шага (пример)
    private void updateStepUI(int stepNumber, boolean completed) {
        // Реализация метода обновления UI шага
    }
}