package com.example.ultai.ultai.api;

import com.example.ultai.ultai.model.GptRequest;
import com.example.ultai.ultai.model.GptResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GptApi {
    @POST("chat/completions")
    Call<GptResponse> getChatCompletion(@Body GptRequest request);
} 