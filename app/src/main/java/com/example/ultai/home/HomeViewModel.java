package com.example.ultai.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<String>> items = new MutableLiveData<>();

    public HomeViewModel() {
        loadItems();
    }

    public LiveData<List<String>> getItems() {
        return items;
    }

    private void loadItems() {
        // Здесь вы можете загрузить данные из сети или базы данных
        items.setValue(Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5"));
    }
}