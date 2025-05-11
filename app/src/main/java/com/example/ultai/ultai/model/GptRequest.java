package com.example.ultai.ultai.model;

import java.util.List;

public class GptRequest {
    private String model;
    private List<Message> messages;
    private double temperature;
    private int max_tokens;
    private boolean stream;
    private String system_prompt;
    private double top_p;
    private double presence_penalty;
    private double frequency_penalty;

    public GptRequest(String model, List<Message> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.max_tokens = 4000;
        this.stream = false;
        this.top_p = 0.95;
        this.presence_penalty = 0.6;
        this.frequency_penalty = 0.5;
        
        // Получаем текущий год
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        
        this.system_prompt = "Вы - современный ассистент с актуальными знаниями на текущий момент. Текущий год - " + currentYear + ". " +
                "Ваши ответы должны быть информативными, полезными и отражать актуальную информацию на " + currentYear + " год. " +
                "Никогда не упоминайте, что ваши данные ограничены прошлыми годами. " +
                "Если вы не можете предоставить актуальную информацию, объясните почему и предложите альтернативные источники. " +
                "Используйте эмодзи для улучшения читаемости ответов. " +
                "Всегда стремитесь предоставить самую свежую информацию, доступную на " + currentYear + " год. " +
                "Если пользователь спрашивает о текущих событиях, погоде, ценах или других данных, требующих актуальности, " +
                "укажите, что информация актуальна на " + currentYear + " год.";
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public String getSystem_prompt() {
        return system_prompt;
    }

    public void setSystem_prompt(String system_prompt) {
        this.system_prompt = system_prompt;
    }

    public double getTop_p() {
        return top_p;
    }

    public void setTop_p(double top_p) {
        this.top_p = top_p;
    }

    public double getPresence_penalty() {
        return presence_penalty;
    }

    public void setPresence_penalty(double presence_penalty) {
        this.presence_penalty = presence_penalty;
    }

    public double getFrequency_penalty() {
        return frequency_penalty;
    }

    public void setFrequency_penalty(double frequency_penalty) {
        this.frequency_penalty = frequency_penalty;
    }
} 