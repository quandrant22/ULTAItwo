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

public class SalesStatisticsFragment extends BasePlannerQuestionFragment {

    private EditText salesStatisticsEditText;
    private static final String KEY_SALES_STATISTICS = "salesStatistics";

    @Override
    protected int getNextFragmentId() {
        return R.id.action_salesStatisticsFragment_to_completionFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_salesStatisticsFragment_to_conversionAnalysisFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Статистика продаж";
    }

    @Override
    protected void saveData() {
        if (salesStatisticsEditText != null) {
            String data = salesStatisticsEditText.getText().toString().trim();
            viewModel.updateAnswer(KEY_SALES_STATISTICS, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_sales_statistics, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        
        salesStatisticsEditText = questionSpecificView.findViewById(R.id.edittext_sales_statistics);
        
        // Подгружаем сохраненные данные, если они есть
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.containsKey(KEY_SALES_STATISTICS)) {
                String savedData = (String) data.get(KEY_SALES_STATISTICS);
                if (savedData != null) {
                    salesStatisticsEditText.setText(savedData);
                }
            }
        });
        
        return rootView;
    }
} 