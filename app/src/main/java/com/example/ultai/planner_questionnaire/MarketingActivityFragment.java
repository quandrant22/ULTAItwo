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

public class MarketingActivityFragment extends BasePlannerQuestionFragment {

    private EditText marketingActivityEditText;
    private static final String KEY_MARKETING_ACTIVITY = "marketingActivity";

    @Override
    protected int getNextFragmentId() {
        return R.id.action_marketingActivityFragment_to_pastMarketingActivityFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_marketingActivityFragment_to_companyAnalysisFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Маркетинговая активность";
    }

    @Override
    protected void saveData() {
        if (marketingActivityEditText != null) {
            String data = marketingActivityEditText.getText().toString().trim();
            viewModel.updateAnswer(KEY_MARKETING_ACTIVITY, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_marketing_activity, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        
        marketingActivityEditText = questionSpecificView.findViewById(R.id.edittext_marketing_activity);
        
        // Подгружаем сохраненные данные, если они есть
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.containsKey(KEY_MARKETING_ACTIVITY)) {
                String savedData = (String) data.get(KEY_MARKETING_ACTIVITY);
                if (savedData != null) {
                    marketingActivityEditText.setText(savedData);
                }
            }
        });
        
        return rootView;
    }
} 