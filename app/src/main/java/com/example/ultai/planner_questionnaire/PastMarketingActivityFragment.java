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

public class PastMarketingActivityFragment extends BasePlannerQuestionFragment {

    private EditText pastMarketingActivityEditText;

    @Override
    protected int getNextFragmentId() {
        return R.id.action_pastMarketingActivityFragment_to_marketingAndAdResourcesFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_pastMarketingActivityFragment_to_marketingActivityFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Прошлая маркетинговая деятельность";
    }

    /*
    @Override
    protected void saveData() {
        String data = pastMarketingActivityEditText.getText().toString();
        // TODO: Implement saving logic
    }
    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_past_marketing_activity, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        pastMarketingActivityEditText = questionSpecificView.findViewById(R.id.edittext_past_marketing_activity);
        return rootView;
    }
} 