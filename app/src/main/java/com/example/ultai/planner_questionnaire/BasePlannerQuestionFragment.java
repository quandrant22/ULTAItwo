package com.example.ultai.planner_questionnaire;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BasePlannerQuestionFragment extends Fragment {

    protected NavController navController;
    protected Button nextButton, prevButton, toAiChatButton;
    protected TextView titleTextView; // Для заголовка страницы анкеты

    // Абстрактный метод для получения ID следующего фрагмента в анкете
    protected abstract int getNextFragmentId();

    // Абстрактный метод для получения ID предыдущего фрагмента в анкете (может быть 0, если нет предыдущего)
    protected abstract int getPreviousFragmentId();
    
    // Абстрактный метод для получения заголовка страницы
    protected abstract String getPageTitle();

    // Абстрактный метод для сохранения данных текущей страницы
    // protected abstract void saveData(); // Раскомментируем и реализуем позже

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

        titleTextView = view.findViewById(R.id.planner_question_title); // ID из будущего макета
        nextButton = view.findViewById(R.id.button_next_planner);      // ID из будущего макета
        prevButton = view.findViewById(R.id.button_prev_planner);      // ID из будущего макета
        toAiChatButton = view.findViewById(R.id.button_to_ai_chat); // ID из будущего макета
        
        titleTextView.setText(getPageTitle());

        if (getNextFragmentId() != 0) {
            nextButton.setOnClickListener(v -> {
                // saveData(); // Сохраняем данные перед переходом
                navController.navigate(getNextFragmentId());
            });
        } else {
            // Если это последняя страница (например, "Завершение")
            // Можно изменить текст кнопки или ее поведение (например, "Завершить")
            nextButton.setText("Завершить");
            nextButton.setOnClickListener(v -> {
                // saveData();
                // Действие по завершению анкеты (например, навигация из анкеты)
                // navController.navigate(R.id.action_planner_to_main_or_profile); // Пример
                // Пока что просто закроем анкету (возврат на предыдущий экран в основном графе)
                // Это потребует, чтобы анкета запускалась через action из основного графа
                 if (getActivity() != null) {
                    // Предполагаем, что после анкеты мы хотим вернуться, например, на главный экран приложения
                    // или экран, с которого был вызван планировщик.
                    // Это нужно будет настроить в основном навигационном графе
                    // Пока что просто popBackStack
                    navController.popBackStack(R.id.planner_nav_graph, true); // Выход из графа анкеты
                 }
            });
        }

        if (getPreviousFragmentId() != 0) {
            prevButton.setVisibility(View.VISIBLE);
            prevButton.setOnClickListener(v -> navController.navigate(getPreviousFragmentId()));
        } else {
            prevButton.setVisibility(View.GONE); // Скрываем кнопку "Назад" на первой странице
        }

        toAiChatButton.setOnClickListener(v -> {
            // Навигация к чату с ИИ
            // Предположим, у чата есть ID в основном навигационном графе
            // Это может быть глобальное действие или действие из planner_nav_graph
            // navController.navigate(R.id.action_global_to_ai_chat_fragment); // Пример
            // Пока оставим TODO, так как нужно знать ID фрагмента чата
            // TODO: Implement navigation to AI Chat
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Скрываем BottomNavigationView
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.nav_view); // Предполагаем, что ID вашей BottomNavigationView - nav_view
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Показываем BottomNavigationView, когда покидаем фрагмент анкеты
        // Это может быть не всегда желаемым поведением, если мы переходим на другой фрагмент анкеты
        // Лучше управлять этим в MainActivity или при выходе из графа анкеты.
        // Пока закомментирую, чтобы избежать мерцания при переходах внутри анкеты.
        /*
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
        */
    }
    
    // Метод, который будет вызван из MainActivity для восстановления BottomNav при выходе из графа анкеты
    public static void restoreBottomNavigation(Fragment currentFragment) {
        if (currentFragment.getActivity() != null) {
            // Проверяем, не является ли текущий фрагмент частью анкеты
            // Это можно сделать, проверяя родительский NavController или ID текущего destination
            NavController hostNavController = Navigation.findNavController(currentFragment.requireActivity(), R.id.nav_host_fragment_activity_main);
            if (hostNavController.getCurrentDestination() != null &&
                hostNavController.getCurrentDestination().getParent() != null &&
                hostNavController.getCurrentDestination().getParent().getId() == R.id.planner_nav_graph) {
                // Мы все еще в графе анкеты, ничего не делаем
            } else {
                 BottomNavigationView bottomNav = currentFragment.getActivity().findViewById(R.id.nav_view);
                 if (bottomNav != null) {
                    bottomNav.setVisibility(View.VISIBLE);
                }
            }
        }
    }
} 