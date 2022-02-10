package com.ai2s_lab.gnss_dr.ui.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ai2s_lab.gnss_dr.databinding.FragmentSettingsBinding;
import com.ai2s_lab.gnss_dr.util.Settings;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import org.w3c.dom.Text;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    // UI Elements
    private TextView settings_title;
    private TextView slider_title;

    private Slider slider;

    // settings
//    private Settings settings;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initialize UI elements
        settings_title = binding.settingsTitle;
        slider = binding.settingsSlider;
        slider_title = binding.settingsSliderTitle;

        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                Settings.setUpdateFrequency((int) value);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void invisibleUI(){
        slider_title.setVisibility(View.INVISIBLE);
        slider.setVisibility(View.INVISIBLE);
    }

    public void visibleUI(){
        slider_title.setVisibility(View.VISIBLE);
        slider.setVisibility(View.VISIBLE);

    }

}