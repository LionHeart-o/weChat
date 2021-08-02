package com.example.wechat.Activity.ui.hideout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HideoutViewModel extends ViewModel {

    private MutableLiveData<String> mText;


    public HideoutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is hideout fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}