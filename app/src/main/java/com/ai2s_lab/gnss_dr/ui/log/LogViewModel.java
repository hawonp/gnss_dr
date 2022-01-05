package com.ai2s_lab.gnss_dr.ui.log;

import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class LogViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private MutableLiveData<String> text_title;

    private Button btn_start;
    private Button btn_reset;
    private Button btn_stop;

    public LogViewModel() {
//        mText = new MutableLiveData<>();
//        mText.setValue("This is the log fragment");
        text_title = new MutableLiveData<>();

        text_title.setValue("Logging Values");
    }

    public Button getBtnStart() { return btn_start; }
    public Button getBtnReset() { return btn_reset; }
    public Button getBtnStop() { return btn_stop; }

    public LiveData<String> getTitle() {
        return text_title;
    }

}