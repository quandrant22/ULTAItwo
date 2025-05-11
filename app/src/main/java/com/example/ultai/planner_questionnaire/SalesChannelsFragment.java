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

    /*
    @Override
    protected void saveData() {
        String data = salesChannelsEditText.getText().toString();
        // TODO: Implement saving logic
    }
    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_sales_channels, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        salesChannelsEditText = questionSpecificView.findViewById(R.id.edittext_sales_channels);
        return rootView;
    }
} 