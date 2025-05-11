package com.example.ultai.news.newsapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ultai.news.newsapp.model.NewsItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NewsUpdateWorker extends Worker {

    private static final String TAG = "NewsUpdateWorker";
    private static final String PREFS_NAME = "news_prefs";
    private static final String KEY_NEWS_ITEMS = "news_items";

    public NewsUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Начинаем выполнение задачи обновления новостей");
        
        try {
            // Получаем новости
            NewsParserAdapter newsParserAdapter = new NewsParserAdapter(getApplicationContext());
            Log.d(TAG, "Создан адаптер парсера новостей");
            
            // Получаем последние новости (20 штук)
            Log.d(TAG, "Запрашиваем последние новости...");
            List<NewsItem> newsItems = newsParserAdapter.getLatestNews(20);
            Log.d(TAG, "Получен результат от getLatestNews: " + (newsItems != null ? newsItems.size() : "null") + " новостей");
            
            // Освобождаем ресурсы парсера
            newsParserAdapter.shutdown();
            
            if (newsItems != null && !newsItems.isEmpty()) {
                // Проверяем первые несколько новостей
                for (int i = 0; i < Math.min(3, newsItems.size()); i++) {
                    NewsItem item = newsItems.get(i);
                    Log.d(TAG, "Новость #" + i + ": " + item.getTitle() + ", источник: " + 
                        (item.getSource() != null ? item.getSource().getName() : "null"));
                }
                
                // Сохраняем новости в SharedPreferences
                saveNewsToSharedPreferences(getApplicationContext(), newsItems);
                Log.d(TAG, "Сохранено " + newsItems.size() + " новостей в SharedPreferences");
                return Result.success();
            } else {
                Log.w(TAG, "Не удалось получить новости через парсер");
                return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Ошибка при получении новостей через парсер", e);
            return Result.retry();
        }
    }

    public static void saveNewsToSharedPreferences(Context context, List<NewsItem> newsItems) {
        if (context == null) {
            Log.e(TAG, "Контекст равен null при сохранении новостей в SharedPreferences");
            return;
        }
        
        if (newsItems == null || newsItems.isEmpty()) {
            Log.e(TAG, "Список новостей пуст или равен null при сохранении в SharedPreferences");
            return;
        }
        
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            
            Gson gson = new Gson();
            String json = gson.toJson(newsItems);
            
            if (json != null && !json.isEmpty()) {
                editor.putString(KEY_NEWS_ITEMS, json);
                editor.apply();
                Log.d(TAG, "Новости успешно сохранены в SharedPreferences: " + newsItems.size() + " новостей, размер JSON: " + json.length());
                
                // Проверяем первые несколько новостей
                for (int i = 0; i < Math.min(3, newsItems.size()); i++) {
                    NewsItem item = newsItems.get(i);
                    Log.d(TAG, "Сохранена новость #" + i + ": " + item.getTitle() + ", источник: " + 
                        (item.getSource() != null ? item.getSource().getName() : "null"));
                }
            } else {
                Log.e(TAG, "Не удалось сериализовать новости в JSON");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении новостей в SharedPreferences", e);
        }
    }

    public static List<NewsItem> loadNewsFromSharedPreferences(Context context) {
        if (context == null) {
            Log.e(TAG, "Контекст равен null при загрузке новостей из SharedPreferences");
            return new ArrayList<>();
        }
        
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String json = sharedPreferences.getString(KEY_NEWS_ITEMS, null);
            
            if (json != null && !json.isEmpty()) {
                Log.d(TAG, "Найдены сохраненные новости в SharedPreferences, длина JSON: " + json.length());
                
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<NewsItem>>() {}.getType();
                    List<NewsItem> newsItems = gson.fromJson(json, type);
                    
                    if (newsItems != null && !newsItems.isEmpty()) {
                        Log.d(TAG, "Успешно загружено " + newsItems.size() + " новостей из SharedPreferences");
                        
                        // Проверяем первые несколько новостей
                        for (int i = 0; i < Math.min(3, newsItems.size()); i++) {
                            NewsItem item = newsItems.get(i);
                            Log.d(TAG, "Новость #" + i + ": " + item.getTitle() + 
                                ", источник: " + (item.getSource() != null ? item.getSource().getName() : "null") +
                                ", дата публикации: " + item.getFormattedTime());
                        }
                        
                        return newsItems;
                    } else {
                        Log.w(TAG, "Список новостей пуст после десериализации");
                        return new ArrayList<>();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при десериализации JSON новостей", e);
                    return new ArrayList<>();
                }
            } else {
                Log.w(TAG, "Новости не найдены в SharedPreferences (json = " + json + ")");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке новостей из SharedPreferences", e);
            return new ArrayList<>();
        }
    }
}