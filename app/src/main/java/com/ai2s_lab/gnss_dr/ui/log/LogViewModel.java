package com.ai2s_lab.gnss_dr.ui.log;

import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class LogViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private MutableLiveData<String> text_title;


    public LogViewModel() {
//        mText = new MutableLiveData<>();
//        mText.setValue("This is the log fragment");
        text_title = new MutableLiveData<>();

        text_title.setValue("Logging Values");



    }


    public LiveData<String> getTitle() {
        return text_title;
    }

}