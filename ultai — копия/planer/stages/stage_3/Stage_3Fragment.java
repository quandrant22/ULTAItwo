package com.example.ultai.planer.stages.stage_3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;

public class Stage_3Fragment extends Fragment {

    // Переменные для хранения контейнеров шагов
    private ConstraintLayout step1Container;
    private ConstraintLayout step2Container;
    private ConstraintLayout step3Container;
    private ConstraintLayout step4Container;
    private ConstraintLayout step5Container;
    private ConstraintLayout step6Container;
    private ConstraintLayout step7Container;
    
    // Массив для отслеживания завершенных шагов
    private boolean[] completedSteps = new boolean[7];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализируем базовый макет для этапа 3
        View view = inflater.inflate(R.layout.fragment_stage_1, container, false);
        
        // Изменяем заголовок на "Этап 3"
        TextView titleTextView = view.findViewById(R.id.textView);
        if (titleTextView != null) {
            titleTextView.setText("Этап 3: Анализ конкурентов");
        }
        
        // Инициализация и настройка интерфейса для шагов
        initializeStepsUI(view);
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Настраиваем кнопку возврата к планировщику
        ImageButton backButton = view.findViewById(R.id.imageButton3);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_faza3_stages_to_navigation_planer);
            });
        }
        
        // Настраиваем кнопку перехода к профилю
        ImageButton profileButton = view.findViewById(R.id.imageButton7);
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_faza3_stages_to_profileFragment);
            });
        }
        
        // Проверяем статус выполнения шагов из SharedPreferences
        checkStepsCompletionStatus();
    }
    
    /**
     * Инициализация UI элементов для шагов этапа 3
     */
    private void initializeStepsUI(View view) {
        // Получаем ссылки на контейнеры шагов
        step1Container = view.findViewById(R.id.step_1_container);
        step2Container = view.findViewById(R.id.step_2_container);
        step3Container = view.findViewById(R.id.step_3_container);
        step4Container = view.findViewById(R.id.step_4_container);
        step5Container = view.findViewById(R.id.step_5_container);
        step6Container = view.findViewById(R.id.step_6_container);
        
        // Попытка найти контейнер шага 7
        step7Container = view.findViewById(R.id.step_7_container);

        // Если седьмой контейнер не найден, то создаем его программно
        if (step7Container == null) {
            addSeventhStep(view);
        }
        
        // Настраиваем обработчики событий для шагов
        setupStepClickListeners();
        
        // Инициализация текстов шагов для этапа 3
        setupStepTexts();
    }
    
    /**
     * Добавление седьмого шага программно
     */
    private void addSeventhStep(View rootView) {
        try {
            // Находим LinearLayout, в который будем добавлять седьмой шаг
            ViewGroup stepsContainer = rootView.findViewById(R.id.stepsContainer);
            
            if (stepsContainer != null) {
                // Инфлейтим макет седьмого шага
                LayoutInflater inflater = LayoutInflater.from(requireContext());
                View step7View = inflater.inflate(R.layout.step_7_layout, stepsContainer, false);
                
                // Добавляем макет в контейнер
                stepsContainer.addView(step7View);
                
                // Получаем ссылку на контейнер седьмого шага
                step7Container = step7View.findViewById(R.id.step_7_container);
            } else {
                // В случае, если не удалось найти контейнер для шагов, создаем дублирующий шестой шаг
                if (step6Container != null && step6Container.getParent() instanceof ViewGroup) {
                    ViewGroup parent = (ViewGroup) step6Container.getParent();
                    
                    // Клонируем шестой шаг
                    LayoutInflater inflater = LayoutInflater.from(requireContext());
                    View step7View = inflater.inflate(R.layout.step_7_layout, (ViewGroup) parent, false);
                    
                    // Добавляем его после шестого шага
                    int index = parent.indexOfChild(step6Container);
                    parent.addView(step7View, index + 1);
                    
                    // Получаем ссылку на новый контейнер
                    step7Container = step7View.findViewById(R.id.step_7_container);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Настройка текстов для шагов этапа 3
     */
    private void setupStepTexts() {
        try {
            // Устанавливаем заголовки и описания для шагов этапа 3
            setStepText(step1Container, R.id.step_1_title, "Шаг 1");
            setStepText(step1Container, R.id.step_1_desc, "Кто является вашими основными конкурентами на рынке?");
            
            setStepText(step2Container, R.id.step_2_title, "Шаг 2");
            setStepText(step2Container, R.id.step_2_desc, "Чем выделяются ваши конкуренты и какие продукты/услуги они предлагают?");
            
            setStepText(step3Container, R.id.step_3_title, "Шаг 3");
            setStepText(step3Container, R.id.step_3_desc, "Каковы сильные и слабые стороны конкурентов, их репутация на рынке?");
            
            setStepText(step4Container, R.id.step_4_title, "Шаг 4");
            setStepText(step4Container, R.id.step_4_desc, "Какие маркетинговые стратегии и каналы привлечения используют конкуренты?");
            
            setStepText(step5Container, R.id.step_5_title, "Шаг 5");
            setStepText(step5Container, R.id.step_5_desc, "В чем заключается ваше уникальное торговое предложение и какова ЦА ваших конкурентов?");
            
            setStepText(step6Container, R.id.step_6_title, "Шаг 6");
            setStepText(step6Container, R.id.step_6_desc, "Какие изменения в конкурентной среде произошли за последнее время?");
            
            if (step7Container != null) {
                setStepText(step7Container, R.id.step_7_title, "Шаг 7");
                setStepText(step7Container, R.id.step_7_desc, "Какие возможности для сотрудничества с конкурентами вы видите?");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Вспомогательный метод для установки текста в TextView внутри контейнера
     */
    private void setStepText(ConstraintLayout container, int textViewId, String text) {
        if (container != null) {
            TextView textView = container.findViewById(textViewId);
            if (textView != null) {
                textView.setText(text);
            }
        }
    }
    
    /**
     * Настройка обработчиков нажатий для шагов
     */
    private void setupStepClickListeners() {
        // Шаг 1
        if (step1Container != null) {
            step1Container.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_faza3_stages_to_step1Stage3Fragment);
            });
        }

        // Шаг 2 (доступен, если выполнен шаг 1)
        if (step2Container != null) {
            step2Container.setOnClickListener(v -> {
                if (completedSteps[0]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step2Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните Шаг 1", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 3 (доступен, если выполнены шаги 1 и 2)
        if (step3Container != null) {
            step3Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step3Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 4 (доступен, если выполнены шаги 1, 2 и 3)
        if (step4Container != null) {
            step4Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1] && completedSteps[2]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step4Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 5 (доступен, если выполнены шаги 1-4)
        if (step5Container != null) {
            step5Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step5Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 6 (доступен, если выполнены шаги 1-5)
        if (step6Container != null) {
            step6Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3] && completedSteps[4]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step6Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 7 (доступен, если выполнены шаги 1-6)
        if (step7Container != null) {
            step7Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3] && completedSteps[4] && completedSteps[5]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step7Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Проверяет статус выполнения всех шагов из SharedPreferences
     */
    private void checkStepsCompletionStatus() {
        // Получаем SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("steps_completion_stage3", 0);
        
        // Проверяем все шаги по порядку
        completedSteps[0] = prefs.getBoolean("stage3_step1_completed", false);
        completedSteps[1] = prefs.getBoolean("stage3_step2_completed", false);
        completedSteps[2] = prefs.getBoolean("stage3_step3_completed", false);
        completedSteps[3] = prefs.getBoolean("stage3_step4_completed", false);
        completedSteps[4] = prefs.getBoolean("stage3_step5_completed", false);
        completedSteps[5] = prefs.getBoolean("stage3_step6_completed", false);
        completedSteps[6] = prefs.getBoolean("stage3_step7_completed", false);
        
        // Обновляем UI в соответствии с выполненными шагами
        updateStepsUI();
    }
    
    /**
     * Обновляет UI шагов в зависимости от их статуса выполнения
     */
    private void updateStepsUI() {
        // Цвета для выполненных и невыполненных шагов
        int completedStepBackground = getResources().getColor(R.color.dark_blue, null);
        int uncompletedStepBackground = getResources().getColor(R.color.dark_gray, null);
        
        // Обновляем внешний вид шагов в зависимости от статуса выполнения
        if (step1Container != null) {
            step1Container.setBackgroundColor(completedSteps[0] ? completedStepBackground : uncompletedStepBackground);
        }
        
        if (step2Container != null) {
            step2Container.setBackgroundColor(completedSteps[1] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 2 доступен только если выполнен шаг 1
            step2Container.setAlpha(completedSteps[0] ? 1.0f : 0.5f);
        }
        
        if (step3Container != null) {
            step3Container.setBackgroundColor(completedSteps[2] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 3 доступен только если выполнены шаги 1 и 2
            step3Container.setAlpha((completedSteps[0] && completedSteps[1]) ? 1.0f : 0.5f);
        }
        
        if (step4Container != null) {
            step4Container.setBackgroundColor(completedSteps[3] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 4 доступен только если выполнены шаги 1, 2 и 3
            step4Container.setAlpha((completedSteps[0] && completedSteps[1] && completedSteps[2]) ? 1.0f : 0.5f);
        }
        
        if (step5Container != null) {
            step5Container.setBackgroundColor(completedSteps[4] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 5 доступен только если выполнены шаги 1-4
            step5Container.setAlpha((completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3]) ? 1.0f : 0.5f);
        }
        
        if (step6Container != null) {
            step6Container.setBackgroundColor(completedSteps[5] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 6 доступен только если выполнены шаги 1-5
            step6Container.setAlpha((completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3] && completedSteps[4]) ? 1.0f : 0.5f);
        }
        
        if (step7Container != null) {
            step7Container.setBackgroundColor(completedSteps[6] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 7 доступен только если выполнены шаги 1-6
            step7Container.setAlpha((completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3] && completedSteps[4] && completedSteps[5]) ? 1.0f : 0.5f);
        }
    }
}

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;

public class Stage_3Fragment extends Fragment {

    // Переменные для хранения контейнеров шагов
    private ConstraintLayout step1Container;
    private ConstraintLayout step2Container;
    private ConstraintLayout step3Container;
    private ConstraintLayout step4Container;
    private ConstraintLayout step5Container;
    private ConstraintLayout step6Container;
    private ConstraintLayout step7Container;
    
    // Массив для отслеживания завершенных шагов
    private boolean[] completedSteps = new boolean[7];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализируем базовый макет для этапа 3
        View view = inflater.inflate(R.layout.fragment_stage_1, container, false);
        
        // Изменяем заголовок на "Этап 3"
        TextView titleTextView = view.findViewById(R.id.textView);
        if (titleTextView != null) {
            titleTextView.setText("Этап 3: Анализ конкурентов");
        }
        
        // Инициализация и настройка интерфейса для шагов
        initializeStepsUI(view);
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Настраиваем кнопку возврата к планировщику
        ImageButton backButton = view.findViewById(R.id.imageButton3);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_faza3_stages_to_navigation_planer);
            });
        }
        
        // Настраиваем кнопку перехода к профилю
        ImageButton profileButton = view.findViewById(R.id.imageButton7);
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_faza3_stages_to_profileFragment);
            });
        }
        
        // Проверяем статус выполнения шагов из SharedPreferences
        checkStepsCompletionStatus();
    }
    
    /**
     * Инициализация UI элементов для шагов этапа 3
     */
    private void initializeStepsUI(View view) {
        // Получаем ссылки на контейнеры шагов
        step1Container = view.findViewById(R.id.step_1_container);
        step2Container = view.findViewById(R.id.step_2_container);
        step3Container = view.findViewById(R.id.step_3_container);
        step4Container = view.findViewById(R.id.step_4_container);
        step5Container = view.findViewById(R.id.step_5_container);
        step6Container = view.findViewById(R.id.step_6_container);
        
        // Попытка найти контейнер шага 7
        step7Container = view.findViewById(R.id.step_7_container);

        // Если седьмой контейнер не найден, то создаем его программно
        if (step7Container == null) {
            addSeventhStep(view);
        }
        
        // Настраиваем обработчики событий для шагов
        setupStepClickListeners();
        
        // Инициализация текстов шагов для этапа 3
        setupStepTexts();
    }
    
    /**
     * Добавление седьмого шага программно
     */
    private void addSeventhStep(View rootView) {
        try {
            // Находим LinearLayout, в который будем добавлять седьмой шаг
            ViewGroup stepsContainer = rootView.findViewById(R.id.stepsContainer);
            
            if (stepsContainer != null) {
                // Инфлейтим макет седьмого шага
                LayoutInflater inflater = LayoutInflater.from(requireContext());
                View step7View = inflater.inflate(R.layout.step_7_layout, stepsContainer, false);
                
                // Добавляем макет в контейнер
                stepsContainer.addView(step7View);
                
                // Получаем ссылку на контейнер седьмого шага
                step7Container = step7View.findViewById(R.id.step_7_container);
            } else {
                // В случае, если не удалось найти контейнер для шагов, создаем дублирующий шестой шаг
                if (step6Container != null && step6Container.getParent() instanceof ViewGroup) {
                    ViewGroup parent = (ViewGroup) step6Container.getParent();
                    
                    // Клонируем шестой шаг
                    LayoutInflater inflater = LayoutInflater.from(requireContext());
                    View step7View = inflater.inflate(R.layout.step_7_layout, (ViewGroup) parent, false);
                    
                    // Добавляем его после шестого шага
                    int index = parent.indexOfChild(step6Container);
                    parent.addView(step7View, index + 1);
                    
                    // Получаем ссылку на новый контейнер
                    step7Container = step7View.findViewById(R.id.step_7_container);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Настройка текстов для шагов этапа 3
     */
    private void setupStepTexts() {
        try {
            // Устанавливаем заголовки и описания для шагов этапа 3
            setStepText(step1Container, R.id.step_1_title, "Шаг 1");
            setStepText(step1Container, R.id.step_1_desc, "Кто является вашими основными конкурентами на рынке?");
            
            setStepText(step2Container, R.id.step_2_title, "Шаг 2");
            setStepText(step2Container, R.id.step_2_desc, "Чем выделяются ваши конкуренты и какие продукты/услуги они предлагают?");
            
            setStepText(step3Container, R.id.step_3_title, "Шаг 3");
            setStepText(step3Container, R.id.step_3_desc, "Каковы сильные и слабые стороны конкурентов, их репутация на рынке?");
            
            setStepText(step4Container, R.id.step_4_title, "Шаг 4");
            setStepText(step4Container, R.id.step_4_desc, "Какие маркетинговые стратегии и каналы привлечения используют конкуренты?");
            
            setStepText(step5Container, R.id.step_5_title, "Шаг 5");
            setStepText(step5Container, R.id.step_5_desc, "В чем заключается ваше уникальное торговое предложение и какова ЦА ваших конкурентов?");
            
            setStepText(step6Container, R.id.step_6_title, "Шаг 6");
            setStepText(step6Container, R.id.step_6_desc, "Какие изменения в конкурентной среде произошли за последнее время?");
            
            if (step7Container != null) {
                setStepText(step7Container, R.id.step_7_title, "Шаг 7");
                setStepText(step7Container, R.id.step_7_desc, "Какие возможности для сотрудничества с конкурентами вы видите?");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Вспомогательный метод для установки текста в TextView внутри контейнера
     */
    private void setStepText(ConstraintLayout container, int textViewId, String text) {
        if (container != null) {
            TextView textView = container.findViewById(textViewId);
            if (textView != null) {
                textView.setText(text);
            }
        }
    }
    
    /**
     * Настройка обработчиков нажатий для шагов
     */
    private void setupStepClickListeners() {
        // Шаг 1
        if (step1Container != null) {
            step1Container.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_faza3_stages_to_step1Stage3Fragment);
            });
        }

        // Шаг 2 (доступен, если выполнен шаг 1)
        if (step2Container != null) {
            step2Container.setOnClickListener(v -> {
                if (completedSteps[0]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step2Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните Шаг 1", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 3 (доступен, если выполнены шаги 1 и 2)
        if (step3Container != null) {
            step3Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step3Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 4 (доступен, если выполнены шаги 1, 2 и 3)
        if (step4Container != null) {
            step4Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1] && completedSteps[2]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step4Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 5 (доступен, если выполнены шаги 1-4)
        if (step5Container != null) {
            step5Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step5Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 6 (доступен, если выполнены шаги 1-5)
        if (step6Container != null) {
            step6Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3] && completedSteps[4]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step6Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Шаг 7 (доступен, если выполнены шаги 1-6)
        if (step7Container != null) {
            step7Container.setOnClickListener(v -> {
                if (completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3] && completedSteps[4] && completedSteps[5]) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_faza3_stages_to_step7Stage3Fragment);
                } else {
                    Toast.makeText(requireContext(), "Сначала выполните предыдущие шаги", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Проверяет статус выполнения всех шагов из SharedPreferences
     */
    private void checkStepsCompletionStatus() {
        // Получаем SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("steps_completion_stage3", 0);
        
        // Проверяем все шаги по порядку
        completedSteps[0] = prefs.getBoolean("stage3_step1_completed", false);
        completedSteps[1] = prefs.getBoolean("stage3_step2_completed", false);
        completedSteps[2] = prefs.getBoolean("stage3_step3_completed", false);
        completedSteps[3] = prefs.getBoolean("stage3_step4_completed", false);
        completedSteps[4] = prefs.getBoolean("stage3_step5_completed", false);
        completedSteps[5] = prefs.getBoolean("stage3_step6_completed", false);
        completedSteps[6] = prefs.getBoolean("stage3_step7_completed", false);
        
        // Обновляем UI в соответствии с выполненными шагами
        updateStepsUI();
    }
    
    /**
     * Обновляет UI шагов в зависимости от их статуса выполнения
     */
    private void updateStepsUI() {
        // Цвета для выполненных и невыполненных шагов
        int completedStepBackground = getResources().getColor(R.color.dark_blue, null);
        int uncompletedStepBackground = getResources().getColor(R.color.dark_gray, null);
        
        // Обновляем внешний вид шагов в зависимости от статуса выполнения
        if (step1Container != null) {
            step1Container.setBackgroundColor(completedSteps[0] ? completedStepBackground : uncompletedStepBackground);
        }
        
        if (step2Container != null) {
            step2Container.setBackgroundColor(completedSteps[1] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 2 доступен только если выполнен шаг 1
            step2Container.setAlpha(completedSteps[0] ? 1.0f : 0.5f);
        }
        
        if (step3Container != null) {
            step3Container.setBackgroundColor(completedSteps[2] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 3 доступен только если выполнены шаги 1 и 2
            step3Container.setAlpha((completedSteps[0] && completedSteps[1]) ? 1.0f : 0.5f);
        }
        
        if (step4Container != null) {
            step4Container.setBackgroundColor(completedSteps[3] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 4 доступен только если выполнены шаги 1, 2 и 3
            step4Container.setAlpha((completedSteps[0] && completedSteps[1] && completedSteps[2]) ? 1.0f : 0.5f);
        }
        
        if (step5Container != null) {
            step5Container.setBackgroundColor(completedSteps[4] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 5 доступен только если выполнены шаги 1-4
            step5Container.setAlpha((completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3]) ? 1.0f : 0.5f);
        }
        
        if (step6Container != null) {
            step6Container.setBackgroundColor(completedSteps[5] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 6 доступен только если выполнены шаги 1-5
            step6Container.setAlpha((completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3] && completedSteps[4]) ? 1.0f : 0.5f);
        }
        
        if (step7Container != null) {
            step7Container.setBackgroundColor(completedSteps[6] ? completedStepBackground : uncompletedStepBackground);
            // Шаг 7 доступен только если выполнены шаги 1-6
            step7Container.setAlpha((completedSteps[0] && completedSteps[1] && completedSteps[2] && completedSteps[3] && completedSteps[4] && completedSteps[5]) ? 1.0f : 0.5f);
        }
    }
}
