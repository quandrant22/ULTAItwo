package com.example.ultai.planner_questionnaire;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ultai20.R;
// Импортируем UserRepository для сохранения данных (понадобится позже)
// import com.example.ultai.data.UserRepository;
// import com.google.firebase.auth.FirebaseAuth;

public class BusinessGoalFragment extends BasePlannerQuestionFragment {

    private EditText businessGoalEditText;
    private static final String KEY_BUSINESS_GOAL = "businessGoal";

    @Override
    protected int getNextFragmentId() {
        // ID действия навигации из planner_nav_graph.xml
        return R.id.action_businessGoalFragment_to_salesAnalysisFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return 0; // Это первый вопрос
    }

    @Override
    protected String getPageTitle() {
        return "Цель бизнеса";
    }

    @Override
    protected void saveData() {
        if (businessGoalEditText != null) {
            String goal = businessGoalEditText.getText().toString().trim();
            viewModel.updateAnswer(KEY_BUSINESS_GOAL, goal);
        }
    }

    /* Раскомментируем, когда будет готова логика сохранения
    @Override
    protected void saveData() {
        String goal = businessGoalEditText.getText().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId != null && !goal.isEmpty()) {
            // UserRepository.getInstance().savePlannerData(userId, "businessGoal", goal, callback...);
            // TODO: Implement saving
        }
    }
    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Сначала получаем View от базового класса (который загружает fragment_base_planner_question.xml)
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        // Находим контейнер для контента в макете базового фрагмента
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        
        // Загружаем специфичный для этого вопроса макет
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_business_goal, contentContainer, false);
        
        // Добавляем специфичный макет в контейнер
        contentContainer.addView(questionSpecificView);
        
        businessGoalEditText = questionSpecificView.findViewById(R.id.edittext_business_goal);
        
        // Подгружаем сохраненные данные, если они есть
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.containsKey(KEY_BUSINESS_GOAL)) {
                String savedGoal = (String) data.get(KEY_BUSINESS_GOAL);
                if (savedGoal != null) {
                    businessGoalEditText.setText(savedGoal);
                }
            }
        });
        
        return rootView;
    }
} 