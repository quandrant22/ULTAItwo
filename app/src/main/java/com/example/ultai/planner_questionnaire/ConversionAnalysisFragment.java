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

public class ConversionAnalysisFragment extends BasePlannerQuestionFragment {

    private EditText conversionAnalysisEditText;

    @Override
    protected int getNextFragmentId() {
        return R.id.action_conversionAnalysisFragment_to_salesStatisticsFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_conversionAnalysisFragment_to_customerFeedbackFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Анализ конверсий";
    }

    /*
    @Override
    protected void saveData() {
        String data = conversionAnalysisEditText.getText().toString();
        // TODO: Implement saving logic
    }
    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_conversion_analysis, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        conversionAnalysisEditText = questionSpecificView.findViewById(R.id.edittext_conversion_analysis);
        return rootView;
    }
} 