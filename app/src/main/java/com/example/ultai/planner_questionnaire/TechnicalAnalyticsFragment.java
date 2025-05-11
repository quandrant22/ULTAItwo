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

public class TechnicalAnalyticsFragment extends BasePlannerQuestionFragment {

    private EditText technicalAnalyticsEditText;

    @Override
    protected int getNextFragmentId() {
        return R.id.action_technicalAnalyticsFragment_to_customerFeedbackFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_technicalAnalyticsFragment_to_salesChannelsFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Техническая аналитика";
    }

    /*
    @Override
    protected void saveData() {
        String data = technicalAnalyticsEditText.getText().toString();
        // TODO: Implement saving logic
    }
    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_technical_analytics, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        technicalAnalyticsEditText = questionSpecificView.findViewById(R.id.edittext_technical_analytics);
        return rootView;
    }
} 