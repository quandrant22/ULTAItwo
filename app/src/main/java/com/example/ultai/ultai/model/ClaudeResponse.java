package com.example.ultai.ultai.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Класс для обработки ответов от API Claude
 * Документация: https://docs.anthropic.com/claude/reference/messages_post
 */
public class ClaudeResponse {
    @SerializedName("id")
    private String id;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("content")
    private List<Content> content;
    
    @SerializedName("model")
    private String model;
    
    @SerializedName("stop_reason")
    private String stopReason;
    
    @SerializedName("stop_sequence")
    private String stopSequence;
    
    @SerializedName("usage")
    private Usage usage;

    /**
     * Класс для представления содержимого ответа
     */
    public static class Content {
        @SerializedName("type")
        private String type;
        
        @SerializedName("text")
        private String text;

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * Класс для представления информации об использовании токенов
     */
    public static class Usage {
        @SerializedName("input_tokens")
        private int inputTokens;
        
        @SerializedName("output_tokens")
        private int outputTokens;

        public int getInputTokens() {
            return inputTokens;
        }

        public int getOutputTokens() {
            return outputTokens;
        }
        
        public int getTotalTokens() {
            return inputTokens + outputTokens;
        }
    }

    // Геттеры
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getRole() {
        return role;
    }

    public List<Content> getContent() {
        return content;
    }

    public String getModel() {
        return model;
    }

    public String getStopReason() {
        return stopReason;
    }

    public String getStopSequence() {
        return stopSequence;
    }

    public Usage getUsage() {
        return usage;
    }
    
    /**
     * Получить текст ответа
     */
    public String getContentText() {
        if (content != null && !content.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (Content item : content) {
                if ("text".equals(item.getType()) && item.getText() != null) {
                    result.append(item.getText());
                }
            }
            return result.toString();
        }
        return "";
    }
} 