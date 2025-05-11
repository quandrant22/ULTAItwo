package com.example.ultai.ultai.api;

import com.example.ultai.ultai.model.DeepSeekRequest;
import com.example.ultai.ultai.model.DeepSeekResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DeepSeekApiService {
    @Headers({
            "Authorization: Bearer sk-7a2c4cbedbfe42dcab4a5fe41b7e60cd",
            "Content-Type: application/json"
    })
    @POST("chat/completions")
    Call<DeepSeekResponse> generateResponse(@Body DeepSeekRequest request);
}