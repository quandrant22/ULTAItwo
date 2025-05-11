package com.example.ultai.planer.stages.stage_1;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Stage_1ViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public Stage_1ViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Машины шишки");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
