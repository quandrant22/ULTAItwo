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

public class SalesAnalysisFragment extends BasePlannerQuestionFragment {

    private EditText salesAnalysisEditText;
    private static final String KEY_SALES_ANALYSIS = "salesAnalysis";

    @Override
    protected int getNextFragmentId() {
        // ID action для перехода к CompanyAnalysisFragment (будет определен в nav_graph)
        return R.id.action_salesAnalysisFragment_to_companyAnalysisFragment; 
    }

    @Override
    protected int getPreviousFragmentId() {
        // ID action для перехода к BusinessGoalFragment (будет определен в nav_graph)
        return R.id.action_salesAnalysisFragment_to_businessGoalFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Анализ продаж";
    }

    @Override
    protected void saveData() {
        if (salesAnalysisEditText != null) {
            String data = salesAnalysisEditText.getText().toString().trim();
            viewModel.updateAnswer(KEY_SALES_ANALYSIS, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_sales_analysis, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        
        salesAnalysisEditText = questionSpecificView.findViewById(R.id.edittext_sales_analysis);
        
        // Подгружаем сохраненные данные, если они есть
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.containsKey(KEY_SALES_ANALYSIS)) {
                String savedData = (String) data.get(KEY_SALES_ANALYSIS);
                if (savedData != null) {
                    salesAnalysisEditText.setText(savedData);
                }
            }
        });
        
        return rootView;
    }
} 