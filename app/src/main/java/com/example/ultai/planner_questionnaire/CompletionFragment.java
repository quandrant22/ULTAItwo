package com.example.ultai.planner_questionnaire;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ultai20.R;

public class CompletionFragment extends BasePlannerQuestionFragment {

    @Override
    protected int getNextFragmentId() {
        return 0; // Это последний фрагмент, кнопка "Вперед" будет "Завершить"
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_completionFragment_to_salesStatisticsFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Завершение анкеты";
    }

    /*
    @Override
    protected void saveData() {
        // На этой странице нет данных для сохранения, но можно инициировать общее сохранение анкеты
        // или просто подтвердить завершение.
    }
    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_completion, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        // Здесь нет EditText, так как это страница завершения
        return rootView;
    }
} 