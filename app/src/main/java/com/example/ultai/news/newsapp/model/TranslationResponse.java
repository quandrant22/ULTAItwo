package com.example.ultai.news.newsapp.model;

import java.util.List;

public class TranslationResponse {
    private List<String> text;

    public List<String> getText() {
        return text;
    }

    public void setText(List<String> text) {
        this.text = text;
    }
}