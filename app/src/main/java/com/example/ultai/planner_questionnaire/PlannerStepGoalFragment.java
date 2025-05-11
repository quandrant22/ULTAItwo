package com.example.ultai.planner_questionnaire;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.Button; // Больше не нужны отдельные Button, т.к. есть binding
// import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

// Используем правильное имя биндинга, если XML файл был переименован в fragment_planner_step_goal.xml,
// то и класс биндинга должен быть FragmentPlannerStepGoalBinding. 
// Пока что оставляю старое, т.к. файл XML не переименовывался, только его содержимое.
import com.example.ultai20.databinding.FragmentPlannerQuestionnaireBinding;


// РЕКОМЕНДУЕТСЯ ПЕРЕИМЕНОВАТЬ ЭТОТ КЛАСС В PlannerStepGoalFragment
public class PlannerStepGoalFragment extends Fragment {

    private static final String TAG = "PlannerStepGoalFrag"; // Изменен TAG для ясности
    private FragmentPlannerQuestionnaireBinding binding;
    private PlannerQuestionnaireViewModel viewModel;
    private NavController navController;

    // Ключ для сохранения/загрузки данных этого шага
    private static final String KEY_BUSINESS_GOAL = "business_goal";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPlannerQuestionnaireBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(PlannerQuestionnaireViewModel.class);

        // Настройка заголовка (если он не задан в XML статически)
        // binding.textViewStepTitle.setText("Цель бизнеса"); // Уже задано в XML

        // Кнопка Назад - на первом шаге скрыта
        binding.buttonBack.setVisibility(View.GONE);

        binding.buttonNext.setOnClickListener(v -> {
            String businessGoal = binding.editTextBusinessGoal.getText().toString().trim();
            if (businessGoal.isEmpty()) {
                binding.inputLayoutBusinessGoal.setError("Пожалуйста, опишите цель вашего бизнеса.");
                // Toast.makeText(getContext(), "Пожалуйста, опишите цель вашего бизнеса.", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.inputLayoutBusinessGoal.setError(null); // Сброс ошибки

            Log.d(TAG, "Сохранение цели бизнеса: " + businessGoal);
            viewModel.updateAnswer(KEY_BUSINESS_GOAL, businessGoal);
            
            // TODO: Навигация на следующий шаг (например, Анализ продаж)
            // navController.navigate(R.id.action_plannerStepGoalFragment_to_plannerStepSalesAnalysisFragment);
            Toast.makeText(getContext(), "Переход к следующему шагу (пока не реализован)", Toast.LENGTH_SHORT).show();
        });

        binding.buttonToAIChat.setOnClickListener(v -> {
            // TODO: Создать action в mobile_navigation.xml для перехода к UltaiFragment
            // Например, action_plannerStepGoalFragment_to_ultaiFragment
            // navController.navigate(R.id.action_plannerStepGoalFragment_to_ultaiFragment);
            Toast.makeText(getContext(), "Переход к чату ИИ (пока не реализован)", Toast.LENGTH_SHORT).show();
        });

        // Убираем старую логику сохранения всей анкеты и навигации отсюда
        // viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> { ... });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Ошибка ViewModel: " + error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonNext.setEnabled(!isLoading);
            binding.buttonBack.setEnabled(!isLoading);
            binding.buttonToAIChat.setEnabled(!isLoading);
        });
        
        // Загрузка существующих данных для этого шага
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.containsKey(KEY_BUSINESS_GOAL)) {
                binding.editTextBusinessGoal.setText(String.valueOf(data.get(KEY_BUSINESS_GOAL)));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; 
    }
} 