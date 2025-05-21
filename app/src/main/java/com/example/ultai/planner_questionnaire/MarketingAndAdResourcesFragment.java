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

public class MarketingAndAdResourcesFragment extends BasePlannerQuestionFragment {

    private EditText marketingAdResourcesEditText;
    private static final String KEY_MARKETING_AD_RESOURCES = "marketingAdResources";

    @Override
    protected int getNextFragmentId() {
        return R.id.action_marketingAndAdResourcesFragment_to_budgetAndResourcesFragment;
    }

    @Override
    protected int getPreviousFragmentId() {
        return R.id.action_marketingAndAdResourcesFragment_to_pastMarketingActivityFragment;
    }

    @Override
    protected String getPageTitle() {
        return "Маркетинговые и рекламные ресурсы";
    }

    @Override
    protected void saveData() {
        if (marketingAdResourcesEditText != null) {
            String data = marketingAdResourcesEditText.getText().toString().trim();
            viewModel.updateAnswer(KEY_MARKETING_AD_RESOURCES, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout contentContainer = rootView.findViewById(R.id.planner_question_content_container);
        View questionSpecificView = inflater.inflate(R.layout.fragment_planner_marketing_and_ad_resources, contentContainer, false);
        contentContainer.addView(questionSpecificView);
        
        marketingAdResourcesEditText = questionSpecificView.findViewById(R.id.edittext_marketing_ad_resources);
        
        // Подгружаем сохраненные данные, если они есть
        viewModel.getQuestionnaireData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.containsKey(KEY_MARKETING_AD_RESOURCES)) {
                String savedData = (String) data.get(KEY_MARKETING_AD_RESOURCES);
                if (savedData != null) {
                    marketingAdResourcesEditText.setText(savedData);
                }
            }
        });
        
        return rootView;
    }
} 