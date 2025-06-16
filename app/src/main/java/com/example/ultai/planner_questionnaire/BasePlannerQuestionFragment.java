package com.example.ultai.planner_questionnaire;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BasePlannerQuestionFragment extends Fragment {

    protected NavController navController;
    protected Button nextButton, prevButton, toAiChatButton;
    protected TextView titleTextView; // Для заголовка страницы анкеты
    protected PlannerQuestionnaireViewModel viewModel;

    // Абстрактный метод для получения ID следующего фрагмента в анкете
    protected abstract int getNextFragmentId();

    // Абстрактный метод для получения ID предыдущего фрагмента в анкете (может быть 0, если нет предыдущего)
    protected abstract int getPreviousFragmentId();
    
    // Абстрактный метод для получения заголовка страницы
    protected abstract String getPageTitle();

    // Абстрактный метод для сохранения данных текущей страницы
    protected abstract void saveData();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PlannerQuestionnaireViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Загружаем общий макет для страниц анкеты
        // Предположим, у нас будет макет 'fragment_base_planner_question.xml'
        // Его нужно будет создать
        return inflater.inflate(R.layout.fragment_base_planner_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        titleTextView = view.findViewById(R.id.planner_question_title);
        nextButton = view.findViewById(R.id.button_next_planner);
        prevButton = view.findViewById(R.id.button_prev_planner);
        toAiChatButton = view.findViewById(R.id.button_to_ai_chat);
        
        titleTextView.setText(getPageTitle());
        
        // Показываем информационное сообщение для первого вопроса анкеты планера
        if (getPreviousFragmentId() == 0) {
            // Это первый вопрос анкеты планера
            if (getActivity() != null) {
                android.widget.Toast.makeText(getActivity(), 
                    "Анкета планера поможет настроить персональные рекомендации для вашего бизнеса", 
                    android.widget.Toast.LENGTH_LONG).show();
            }
        }

        if (getNextFragmentId() != 0) {
            nextButton.setOnClickListener(v -> {
                saveData(); // Сохраняем данные перед переходом
                
                try {
                    // Безопасная навигация с обработкой возможных ошибок
                    if (navController.getCurrentDestination() != null) {
                        int currentId = navController.getCurrentDestination().getId();
                        int actionId = getNextFragmentId();
                        
                        // Логируем для отладки
                        Log.d("BasePlannerQuestionFragment", 
                            "Attempting navigation from ID: " + currentId + 
                            " using action ID: " + actionId);
                        
                        // Проверяем, соответствует ли текущий фрагмент ожидаемому фрагменту в planner_nav_graph
                        // Это поможет избежать ошибок, связанных с несоответствием destinationId в графе
                        if (currentId == R.id.businessGoalFragment || 
                            currentId == R.id.salesAnalysisFragment ||
                            currentId == R.id.companyAnalysisFragment || 
                            currentId == R.id.marketingActivityFragment ||
                            currentId == R.id.pastMarketingActivityFragment || 
                            currentId == R.id.marketingAndAdResourcesFragment ||
                            currentId == R.id.budgetAndResourcesFragment || 
                            currentId == R.id.goalsAndExpectationsFragment ||
                            currentId == R.id.salesChannelsFragment || 
                            currentId == R.id.technicalAnalyticsFragment ||
                            currentId == R.id.customerFeedbackFragment || 
                            currentId == R.id.conversionAnalysisFragment ||
                            currentId == R.id.salesStatisticsFragment || 
                            currentId == R.id.completionFragment) {
                            // Фрагмент находится в planner_nav_graph, продолжаем навигацию
                            navController.navigate(actionId);
                        } else if (currentId == R.id.plannerQuestionnaireFragment) {
                            // Если мы находимся в промежуточном фрагменте, переходим в граф анкеты
                            navController.navigate(R.id.action_plannerQuestionnaireFragment_to_businessGoalFragment);
                        } else {
                            // Неожиданный фрагмент, попробуем вернуться к бизнес-цели
                            Log.w("BasePlannerQuestionFragment", "Unexpected current destination: " + currentId);
                            navController.navigate(R.id.businessGoalFragment);
                        }
                    }
                } catch (Exception e) {
                    Log.e("BasePlannerQuestionFragment", "Navigation error", e);
                }
            });
        } else {
            // Если это последняя страница (например, "Завершение")
            nextButton.setText("Завершить");
            nextButton.setOnClickListener(v -> {
                saveData(); // Сохраняем данные
                // Сохраняем все данные анкеты в Firebase
                saveAllDataToFirebase();
            });
        }

        if (getPreviousFragmentId() != 0) {
            prevButton.setVisibility(View.VISIBLE);
            prevButton.setOnClickListener(v -> {
                // При переходе назад тоже сохраняем данные текущей страницы
                saveData();
                
                try {
                    // Безопасная навигация назад
                    if (navController.getCurrentDestination() != null) {
                        int currentId = navController.getCurrentDestination().getId();
                        int actionId = getPreviousFragmentId();
                        
                        // Логируем для отладки
                        Log.d("BasePlannerQuestionFragment", 
                            "Attempting backward navigation from ID: " + currentId + 
                            " using action ID: " + actionId);
                        
                        // Проверяем, находимся ли мы в графе анкеты
                        if (currentId == R.id.businessGoalFragment || 
                            currentId == R.id.salesAnalysisFragment ||
                            currentId == R.id.companyAnalysisFragment || 
                            currentId == R.id.marketingActivityFragment ||
                            currentId == R.id.pastMarketingActivityFragment || 
                            currentId == R.id.marketingAndAdResourcesFragment ||
                            currentId == R.id.budgetAndResourcesFragment || 
                            currentId == R.id.goalsAndExpectationsFragment ||
                            currentId == R.id.salesChannelsFragment || 
                            currentId == R.id.technicalAnalyticsFragment ||
                            currentId == R.id.customerFeedbackFragment || 
                            currentId == R.id.conversionAnalysisFragment ||
                            currentId == R.id.salesStatisticsFragment || 
                            currentId == R.id.completionFragment) {
                            // Выполняем навигацию в пределах графа анкеты
                            navController.navigate(actionId);
                        } else if (currentId == R.id.plannerQuestionnaireFragment) {
                            // Если мы в промежуточном фрагменте, возвращаемся в планировщик
                            navController.navigate(R.id.action_plannerQuestionnaireFragment_to_navigation_planer);
                        } else {
                            // Неожиданное состояние, возвращаемся в планировщик
                            Log.w("BasePlannerQuestionFragment", "Unexpected current destination: " + currentId);
                            navController.navigate(R.id.navigation_planer);
                        }
                    }
                } catch (Exception e) {
                    Log.e("BasePlannerQuestionFragment", "Backward navigation error", e);
                }
            });
        } else {
            prevButton.setVisibility(View.GONE); // Скрываем кнопку "Назад" на первой странице
        }

        toAiChatButton.setOnClickListener(v -> {
            // Сохраняем данные перед переходом к чату с ИИ
            saveData();
            // Навигация к чату с ИИ
            if (navController.getCurrentDestination() != null) {
                int destinationId = navController.getCurrentDestination().getId();
                int actionId = 0;
                
                if (destinationId == R.id.businessGoalFragment) {
                    actionId = R.id.action_businessGoalFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.salesAnalysisFragment) {
                    actionId = R.id.action_salesAnalysisFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.companyAnalysisFragment) {
                    actionId = R.id.action_companyAnalysisFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.marketingActivityFragment) {
                    actionId = R.id.action_marketingActivityFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.pastMarketingActivityFragment) {
                    actionId = R.id.action_pastMarketingActivityFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.marketingAndAdResourcesFragment) {
                    actionId = R.id.action_marketingAndAdResourcesFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.budgetAndResourcesFragment) {
                    actionId = R.id.action_budgetAndResourcesFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.goalsAndExpectationsFragment) {
                    actionId = R.id.action_goalsAndExpectationsFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.salesChannelsFragment) {
                    actionId = R.id.action_salesChannelsFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.technicalAnalyticsFragment) {
                    actionId = R.id.action_technicalAnalyticsFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.customerFeedbackFragment) {
                    actionId = R.id.action_customerFeedbackFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.conversionAnalysisFragment) {
                    actionId = R.id.action_conversionAnalysisFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.salesStatisticsFragment) {
                    actionId = R.id.action_salesStatisticsFragment_to_ultaiFragment2;
                } else if (destinationId == R.id.completionFragment) {
                    actionId = R.id.action_completionFragment_to_ultaiFragment2;
                }
                
                if (actionId != 0) {
                    navController.navigate(actionId);
                }
            }
        });
    }

    private void saveAllDataToFirebase() {
        // Показываем индикатор загрузки или блокируем UI
        if (getActivity() != null) {
            viewModel.saveFinalQuestionnaire();
            
            // Наблюдаем за результатом сохранения
            viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
                if (success) {
                    Log.d("BasePlannerQuestionFragment", "Анкета успешно сохранена, начинаем навигацию к планеру");
                    
                    // Восстанавливаем нижнюю панель навигации перед навигацией
                    if (getActivity() instanceof com.example.ultai20.MainActivity) {
                        ((com.example.ultai20.MainActivity) getActivity()).showNavigationBar();
                        Log.d("BasePlannerQuestionFragment", "Показали нижнюю панель навигации");
                    }
                    
                    // Прямой переход на экран планера в основном графе
                    try {
                        Log.d("BasePlannerQuestionFragment", "Пытаемся перейти к экрану планера");
                        
                        // Получаем NavController основного графа
                        NavController mainNavController = Navigation.findNavController(
                                requireActivity(), R.id.nav_host_fragment_activity_main);
                        
                        // Проверка, что NavController получен
                        if (mainNavController != null) {
                            Log.d("BasePlannerQuestionFragment", "mainNavController получен, переходим к navigation_planer");
                            // Переходим к экрану планера
                            mainNavController.navigate(R.id.navigation_planer);
                        } else {
                            Log.e("BasePlannerQuestionFragment", "mainNavController равен null");
                            // Прямая навигация через действие
                            navController.navigate(R.id.action_plannerQuestionnaireFragment_to_navigation_planer);
                        }
                    } catch (Exception e) {
                        Log.e("BasePlannerQuestionFragment", "Ошибка при навигации к экрану планера", e);
                        
                        // Запасной вариант через popBackStack
                        try {
                            boolean popped = navController.popBackStack(R.id.navigation_planer, false);
                            Log.d("BasePlannerQuestionFragment", "popBackStack к navigation_planer: " + popped);
                            
                            if (!popped) {
                                // Если не удалось, пробуем через action
                                navController.navigate(R.id.action_plannerQuestionnaireFragment_to_navigation_planer);
                                Log.d("BasePlannerQuestionFragment", "Перешли через action_plannerQuestionnaireFragment_to_navigation_planer");
                            }
                        } catch (Exception e2) {
                            Log.e("BasePlannerQuestionFragment", "Ошибка при запасном варианте навигации", e2);
                        }
                    }
                } else {
                    Log.d("BasePlannerQuestionFragment", "Не удалось сохранить анкету");
                }
            });
            
            viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
                if (error != null && !error.isEmpty()) {
                    Log.e("BasePlannerQuestionFragment", "Ошибка при сохранении анкеты: " + error);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Принудительно скрываем BottomNavigationView в onResume
        hideBottomNavigationView();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Не восстанавливаем панель навигации при переходе между фрагментами анкеты
        // Это будет делаться в restoreBottomNavigation при выходе из графа анкеты
    }
    
    // Вспомогательный метод для скрытия нижней панели навигации
    private void hideBottomNavigationView() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                // Устанавливаем невидимую, а не просто GONE, чтобы избежать перекомпоновки
                bottomNav.setVisibility(View.GONE);
                // Дополнительно обеспечиваем, чтобы она не занимала место в иерархии
                bottomNav.setEnabled(false);
            }
            
            // Принудительно скрываем через MainActivity, если это возможно
            try {
                if (getActivity() instanceof com.example.ultai20.MainActivity) {
                    ((com.example.ultai20.MainActivity) getActivity()).hideNavigationBar();
                }
            } catch (Exception e) {
                // Игнорируем исключение, если метод недоступен
            }
        }
    }
    
    // Метод, который будет вызван из MainActivity для восстановления BottomNav при выходе из графа анкеты
    public static void restoreBottomNavigation(Fragment currentFragment) {
        if (currentFragment.getActivity() != null) {
            // Проверяем, не является ли текущий фрагмент частью анкеты
            try {
                NavController hostNavController = Navigation.findNavController(currentFragment.requireActivity(), R.id.nav_host_fragment_activity_main);
                if (hostNavController.getCurrentDestination() != null &&
                    hostNavController.getCurrentDestination().getParent() != null &&
                    hostNavController.getCurrentDestination().getParent().getId() == R.id.planner_nav_graph) {
                    // Мы все еще в графе анкеты, ничего не делаем
                } else {
                    BottomNavigationView bottomNav = currentFragment.getActivity().findViewById(R.id.nav_view);
                    if (bottomNav != null) {
                        bottomNav.setVisibility(View.VISIBLE);
                        bottomNav.setEnabled(true);
                    }
                    
                    // Пытаемся также вызвать метод из MainActivity
                    try {
                        if (currentFragment.getActivity() instanceof com.example.ultai20.MainActivity) {
                            ((com.example.ultai20.MainActivity) currentFragment.getActivity()).showNavigationBar();
                        }
                    } catch (Exception e) {
                        // Игнорируем исключение
                    }
                }
            } catch (Exception e) {
                // Если что-то пошло не так, хотя бы покажем панель навигации
                BottomNavigationView bottomNav = currentFragment.getActivity().findViewById(R.id.nav_view);
                if (bottomNav != null) {
                    bottomNav.setVisibility(View.VISIBLE);
                    bottomNav.setEnabled(true);
                }
            }
        }
    }
} 