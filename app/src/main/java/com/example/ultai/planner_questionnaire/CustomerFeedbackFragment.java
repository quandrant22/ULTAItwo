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

public class CustomerFeedbackFragment extends BasePlannerQuestionFragment {

    private EditText customerFeedbackEditText;
    private static final String KEY_CUSTOMER_FEEDBACK = "customerFeedback";

    @Override
    protected int getNextFragmentId() {
        return R.id.action_customerFeedbackFragment_to_conversionAnalysisFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_customerFeedbackFragment_to_technicalAnalyticsFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Обратная связь от клиентов";
    }

    @Override
    protected void saveData() {
        if (customerFeedbackEditText != null) {
            String data = customerFeedbackEditText.getText().toString().trim();
            viewModel.updateAnswer(KEY_CUSTOMER_FEEDBACK, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_customer_feedback, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        
        customerFeedbackEditText = questionSpecificView.findViewById(R.id.edittext_customer_feedback);
        
        // Подгружаем сохраненные данные, если они есть
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.containsKey(KEY_CUSTOMER_FEEDBACK)) {
                String savedData = (String) data.get(KEY_CUSTOMER_FEEDBACK);
                if (savedData != null) {
                    customerFeedbackEditText.setText(savedData);
                }
            }
        });
        
        return rootView;
    }
} 