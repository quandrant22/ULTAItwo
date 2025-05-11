package com.example.ultai.news;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ultai.news.newsapp.model.NewsItem;

import java.util.List;

public class NewsViewModel extends AndroidViewModel {

    private MutableLiveData<List<NewsItem>> newsLiveData;

    public NewsViewModel(@NonNull Application application) {
        super(application);
        newsLiveData = new MutableLiveData<>();
    }

    // Метод для получения новостей
    public LiveData<List<NewsItem>> getNews() {
        return newsLiveData;
    }

    // Метод для обновления списка новостей
    public void updateNews(List<NewsItem> newsItems) {
        newsLiveData.setValue(newsItems);
    }
}