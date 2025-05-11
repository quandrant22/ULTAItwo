package com.example.ultai.anketa;

public class QuestionnaireItem {
    int id;
    String[] question; // Array to support potentially compound questions
    String type; // Such as "textArea", "select", "dropDown"
    String[] answers; // Possible answers for select or dropdown types
    String title; // Optional title for the questionnaire item
    String[] additionalQuestion; // Optional additional questions related to the main question

    // Constructor
    public QuestionnaireItem(int id, String[] question, String type, String[] answers, String title, String[] additionalQuestion) {
        this.id = id;
        this.question = question;
        this.type = type;
        this.answers = answers;
        this.title = title;
        this.additionalQuestion = additionalQuestion;
    }

    // Getters and Setters (Assuming they might be required)
}
