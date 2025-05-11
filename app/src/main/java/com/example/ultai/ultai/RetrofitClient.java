package com.example.ultai.ultai;

import com.example.ultai.ultai.api.DeepSeekApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.deepseek.ai/v1/";

    public static DeepSeekApiService getApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(DeepSeekApiService.class);
    }
}