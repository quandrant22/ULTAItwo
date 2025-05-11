package com.example.ultai.anketa;

import java.util.ArrayList;
import java.util.List;

public class QuestionnaireManager {
    List<QuestionnaireItem> QuestionnaireOneList;

    public QuestionnaireManager() {
        QuestionnaireOneList = new ArrayList<>();
        populateQuestionnaireList();
    }

    public List<QuestionnaireItem> getQuestionnaireOneList() {
        return QuestionnaireOneList; // Возвращение реального списка
    }


    private void populateQuestionnaireList() {
        QuestionnaireOneList.add(new QuestionnaireItem(
                1,
                new String[]{"What is the name of your company?"},
                "textArea",
                new String[]{"Your Country"}, // No predefined answers for textArea
                "",
                new String[]{}
        ));

        QuestionnaireOneList.add(new QuestionnaireItem(
                2,
                new String[]{"What is the current state of your business?"},
                "select",
                new String[]{"Planning to launch", "Already launched"},
                "My Business",
                new String[]{}
        ));

        QuestionnaireOneList.add(new QuestionnaireItem(
                3,
                new String[]{"Country"},
                "dropDown",
                new String[]{"USA", "Canada", "UK"}, // Example countries
                "",
                new String[]{}
        ));

        QuestionnaireOneList.add(new QuestionnaireItem(
                4,
                new String[]{"What type of products or services do you offer?"},
                "textArea",
                new String[]{}, // No predefined answers for textArea
                "",
                new String[]{}
        ));

        QuestionnaireOneList.add(new QuestionnaireItem(
                5,
                new String[]{"Where do you sell your products or services (market scope)?"},
                "select",
                new String[]{"Local", "National", "International"},
                "",
                new String[]{"Please specify the region or country if international"}
        ));
    }
}
