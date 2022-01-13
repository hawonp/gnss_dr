package com.ai2s_lab.gnss_dr.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ai2s_lab.gnss_dr.R;
import com.ai2s_lab.gnss_dr.databinding.FragmentSettingsBinding;
import com.ai2s_lab.gnss_dr.util.Settings;

import org.w3c.dom.Text;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private FragmentSettingsBinding binding;

    // UI Elements
    private TextView settings_title;
    private RadioButton radio_gnss;
    private RadioButton radio_fused;

    // settings
//    private Settings settings;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initialize settings
//        settings = new Settings();

        // initialize UI elements
        settings_title = binding.settingsTitle;
        radio_gnss = binding.radioBtnGnss;
        radio_fused = binding.radioBtnFused;

        // default values for UI elements
        if(Settings.getGpsChoice() == -1)
            settings_title.setText("GPS Provider Not Selected");
        else if(Settings.getGpsChoice() == 1){
            settings_title.setText("Using FusedLocationProvider");
            radio_fused.toggle();
        } else{
            settings_title.setText("Using GNSS");
            radio_gnss.toggle();
        }

        // button handlers for radio buttons
        radio_fused.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radio_fused.isChecked()){
                    settings_title.setText("Using FusedLocationProvider");
                    Settings.setGpsChoice(1);
                }
            }
        });

        radio_gnss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radio_gnss.isChecked()){
                    settings_title.setText("Using GNSS");
                    Settings.setGpsChoice(2);
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}