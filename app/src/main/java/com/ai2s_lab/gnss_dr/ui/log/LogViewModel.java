package com.ai2s_lab.gnss_dr.ui.log;

import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LogViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private MutableLiveData<String> text_title;
    private MutableLiveData<String> text_subtitle;

    public LogViewModel() {
//        mText = new MutableLiveData<>();
//        mText.setValue("This is the log fragment");
        text_title = new MutableLiveData<>();
        text_subtitle = new MutableLiveData<>();

        text_title.setValue("Logging Values");
        text_subtitle.setValue("This is a random subtitle");

    }


    public LiveData<String> getTitle() {
        return text_title;
    }

    public LiveData<String> getSubtitle() { return text_subtitle; }
}