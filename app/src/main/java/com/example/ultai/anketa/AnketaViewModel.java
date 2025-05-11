package com.example.ultai.anketa;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnketaViewModel extends ViewModel {

    private MutableLiveData<String> mText; // Declare the MutableLiveData field

    public AnketaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("START");
    }

    public AnketaViewModel(MutableLiveData<String> mText) {
        this.mText = mText;
    }

    public LiveData<String> getText() {
        return mText;
    }
}
