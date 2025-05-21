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

public class GoalsAndExpectationsFragment extends BasePlannerQuestionFragment {

    private EditText goalsExpectationsEditText;
    private static final String KEY_GOALS_EXPECTATIONS = "goalsExpectations";

    @Override
    protected int getNextFragmentId() {
        return R.id.action_goalsAndExpectationsFragment_to_salesChannelsFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_goalsAndExpectationsFragment_to_budgetAndResourcesFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Цели и ожидания";
    }

    @Override
    protected void saveData() {
        if (goalsExpectationsEditText != null) {
            String data = goalsExpectationsEditText.getText().toString().trim();
            viewModel.updateAnswer(KEY_GOALS_EXPECTATIONS, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_goals_and_expectations, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        
        goalsExpectationsEditText = questionSpecificView.findViewById(R.id.edittext_goals_expectations);
        
        // Подгружаем сохраненные данные, если они есть
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.containsKey(KEY_GOALS_EXPECTATIONS)) {
                String savedData = (String) data.get(KEY_GOALS_EXPECTATIONS);
                if (savedData != null) {
                    goalsExpectationsEditText.setText(savedData);
                }
            }
        });
        
        return rootView;
    }
} 