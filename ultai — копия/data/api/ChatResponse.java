package com.example.ultai.data.api;

import java.util.List;

public class ChatResponse {
    private String id;
    private List<Choice> choices;
    private Usage usage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public static class Choice {
        private int index;
        private ChatMessage message;
        private String finish_reason;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public ChatMessage getMessage() {
            return message;
        }

        public void setMessage(ChatMessage message) {
            this.message = message;
        }

        public String getFinishReason() {
            return finish_reason;
        }

        public void setFinishReason(String finish_reason) {
            this.finish_reason = finish_reason;
        }
    }

    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;

        public int getPromptTokens() {
            return prompt_tokens;
        }

        public void setPromptTokens(int prompt_tokens) {
            this.prompt_tokens = prompt_tokens;
        }

        public int getCompletionTokens() {
            return completion_tokens;
        }

        public void setCompletionTokens(int completion_tokens) {
            this.completion_tokens = completion_tokens;
        }

        public int getTotalTokens() {
            return total_tokens;
        }

        public void setTotalTokens(int total_tokens) {
            this.total_tokens = total_tokens;
        }
    }
} 