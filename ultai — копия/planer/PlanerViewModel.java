package com.example.ultai.planer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlanerViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PlanerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Машины шишки");
    }

    public LiveData<String> getText() {
        return mText;
    }
}