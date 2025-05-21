package com.example.ultai.planner_questionnaire;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;

public class CompletionFragment extends BasePlannerQuestionFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Сначала получаем View от базового класса (который загружает fragment_base_planner_question.xml)
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        // Находим контейнер для контента в макете базового фрагмента
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        
        // Загружаем специфичный для этого вопроса макет
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_completion, contentContainer, false);
        
        // Добавляем специфичный макет в контейнер
        contentContainer.addView(questionSpecificView);
        
        return rootView;
    }

    @Override
    protected int getNextFragmentId() {
        return 0; // Последний фрагмент
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_completionFragment_to_salesStatisticsFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Завершение анкеты";
    }

    @Override
    protected void saveData() {
        // Для этого фрагмента не нужно сохранять данные
        // Отметить только флаг завершения
        viewModel.updateAnswer("completed", true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Находим TextView для сводки в специфичном макете
        TextView summaryText = view.findViewById(R.id.text_completion_summary);
        
        // Заполняем сводку информацией из основных вопросов
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && summaryText != null) {
                StringBuilder summary = new StringBuilder();
                summary.append("Спасибо за заполнение анкеты!\n\n");
                
                if (data.containsKey("businessGoal")) {
                    summary.append("Цель бизнеса: ").append(data.get("businessGoal")).append("\n\n");
                }
                
                if (data.containsKey("salesAnalysis")) {
                    summary.append("Ключевые выводы по продажам: ").append(data.get("salesAnalysis")).append("\n\n");
                }
                
                summary.append("Ваша анкета будет проанализирована и на основе ответов будет создан план маркетинга. ");
                summary.append("Нажмите 'Завершить', чтобы сохранить данные анкеты.");
                
                summaryText.setText(summary.toString());
            }
        });
        
        // Отслеживаем статус сохранения только один раз при создании представления
        final boolean[] saveObserved = {false};
        
        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            Log.d("CompletionFragment", "Получен статус сохранения: " + success);
            
            if (saveObserved[0]) {
                // Пропускаем повторные события, если уже обработали одно
                Log.d("CompletionFragment", "Пропуск обработки результата, так как уже был обработан");
                return;
            }
            
            if (success) {
                saveObserved[0] = true;
                Log.d("CompletionFragment", "Анкета успешно сохранена, начинаем навигацию");
                
                // Сначала восстановим панель навигации
                if (getActivity() instanceof com.example.ultai20.MainActivity) {
                    ((com.example.ultai20.MainActivity) getActivity()).showNavigationBar();
                }
                
                // Используем Handler для небольшой задержки навигации
                new Handler().postDelayed(() -> {
                    try {
                        // Пытаемся напрямую перейти к экрану планера через главный NavController
                        NavController mainNavController = Navigation.findNavController(
                            requireActivity(), R.id.nav_host_fragment_activity_main);
                        
                        // Переходим к планеру в основном графе
                        mainNavController.navigate(R.id.navigation_planer);
                        
                    } catch (Exception e) {
                        Log.e("CompletionFragment", "Ошибка при прямой навигации к планеру", e);
                        
                        try {
                            // Резервный способ через navController фрагмента
                            navController.popBackStack(R.id.navigation_planer, false);
                        } catch (Exception e2) {
                            Log.e("CompletionFragment", "Ошибка при popBackStack", e2);
                            
                            // Последняя попытка - перейти через action из промежуточного фрагмента
                            try {
                                navController.navigate(R.id.action_plannerQuestionnaireFragment_to_navigation_planer);
                            } catch (Exception e3) {
                                Log.e("CompletionFragment", "Все методы навигации не сработали", e3);
                            }
                        }
                    }
                }, 500); // Уменьшаем задержку до 500 мс, так как нет необходимости показывать Toast
            } else {
                // Убираем Toast об ошибке сохранения
                Log.e("CompletionFragment", "Не удалось сохранить анкету");
            }
        });
        
        // Отслеживаем сообщения об ошибках, но не показываем Toast
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Log.e("CompletionFragment", "Ошибка при сохранении анкеты: " + errorMsg);
            }
        });
        
        // Переопределяем поведение кнопки "Завершить" для более надежной навигации
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                Log.d("CompletionFragment", "Нажата кнопка 'Завершить', начинаем сохранение...");
                
                // Сохраняем данные
                saveData();
                
                // Сохраняем все данные анкеты в Firebase
                viewModel.saveFinalQuestionnaire();
            });
        }
    }
} 