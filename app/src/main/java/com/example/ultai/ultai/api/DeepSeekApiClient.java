package com.example.ultai.ultai.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;

public class DeepSeekApiClient {
    private static final String API_URL = "https://api.deepseek.ai/v1/chat/completions";
    private static final String API_KEY = "sk-4dd3f2872fee474293a6cfde2c70aedf";
    private final OkHttpClient client;
    
    public interface Callback {
        void onSuccess(String aiResponse);
        void onFailure(String error);
    }
    
    public DeepSeekApiClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(10);
        
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS))
                .dispatcher(dispatcher)
                .retryOnConnectionFailure(true)
                .build();
    }

    public void sendMessage(List<JSONObject> chatHistory, Callback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("model", "deepseek-chat");
            json.put("messages", new JSONArray(chatHistory));
            json.put("temperature", 0.7);
            json.put("max_tokens", 2000); // Ограничиваем длину ответа для ускорения
            json.put("top_p", 0.95);
            json.put("presence_penalty", 0.6);
            json.put("frequency_penalty", 0.5);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onFailure(response.message());
                        return;
                    }
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String aiResponse = jsonResponse.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        callback.onSuccess(aiResponse);
                    } catch (Exception e) {
                        callback.onFailure("Ошибка обработки ответа");
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }
    
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
    }
}
