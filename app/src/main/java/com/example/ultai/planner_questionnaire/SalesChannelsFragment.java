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

public class SalesChannelsFragment extends BasePlannerQuestionFragment {

    private EditText salesChannelsEditText;
    private static final String KEY_SALES_CHANNELS = "salesChannels";

    @Override
    protected int getNextFragmentId() {
        return R.id.action_salesChannelsFragment_to_technicalAnalyticsFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_salesChannelsFragment_to_goalsAndExpectationsFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Каналы продаж";
    }

    @Override
    protected void saveData() {
        if (salesChannelsEditText != null) {
            String data = salesChannelsEditText.getText().toString().trim();
            viewModel.updateAnswer(KEY_SALES_CHANNELS, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_sales_channels, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        
        salesChannelsEditText = questionSpecificView.findViewById(R.id.edittext_sales_channels);
        
        // Подгружаем сохраненные данные, если они есть
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.containsKey(KEY_SALES_CHANNELS)) {
                String savedData = (String) data.get(KEY_SALES_CHANNELS);
                if (savedData != null) {
                    salesChannelsEditText.setText(savedData);
                }
            }
        });
        
        return rootView;
    }
} 