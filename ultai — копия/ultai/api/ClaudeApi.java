package com.example.ultai.ultai.api;

import com.example.ultai.ultai.model.ClaudeRequest;
import com.example.ultai.ultai.model.ClaudeResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Интерфейс для работы с API Claude от Anthropic
 */
public interface ClaudeApi {
    @POST("v1/messages")
    Call<ClaudeResponse> sendMessage(@Body ClaudeRequest request);
} 