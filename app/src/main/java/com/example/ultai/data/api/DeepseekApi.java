package com.example.ultai.data.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DeepseekApi {
    @Headers({
        "Content-Type: application/json",
        "Authorization: Bearer sk-4dd3f2872fee474293a6cfde2c70aedf"
    })
    @POST("v1/chat/completions")
    Call<ChatResponse> getChatCompletion(@Body ChatRequest request);
} 