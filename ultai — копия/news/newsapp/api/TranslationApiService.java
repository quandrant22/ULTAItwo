package com.example.ultai.news.newsapp.api;

import com.example.ultai.news.newsapp.model.TranslationResponse;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TranslationApiService {
    @POST("translate")
    @FormUrlEncoded
    Call<TranslationResponse> translate(
            @Query("key") String apiKey,
            @Query("text") String text,
            @Query("lang") String lang
    );
}