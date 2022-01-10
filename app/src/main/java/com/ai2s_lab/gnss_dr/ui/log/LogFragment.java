package com.ai2s_lab.gnss_dr.ui.log;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ai2s_lab.gnss_dr.MainActivity;
import com.ai2s_lab.gnss_dr.R;
import com.ai2s_lab.gnss_dr.databinding.FragmentLogBinding;
import com.ai2s_lab.gnss_dr.gnss.GnssRetriever;
import com.ai2s_lab.gnss_dr.io.Logger;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class LogFragment extends Fragment {

    private LogViewModel logViewModel;
    private FragmentLogBinding binding;
    private Button btn_start;
    private Button btn_reset;
    private Button btn_stop;
    private Switch switch_log;
    private Logger logger;
    private GnssRetriever gnss_retriever;

    private final String LOG = "LOG";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        logViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initialize UI elements
        final TextView text_title = binding.textLogTitle;

        btn_reset = binding.btnLogReset;
        btn_start = binding.btnLogStart;
        btn_stop = binding.btnLogStop;
        switch_log = binding.switchLogTrack;

        // not needed anymore
        logViewModel.getTitle().observe(getViewLifecycleOwner(), s -> {
            text_title.setText(s);
        });

        // states for logging buttons

        btn_stop.setEnabled(false);
        btn_reset.setEnabled(false);
        switch_log.setChecked(false);

        //initialise retriever
        gnss_retriever = new GnssRetriever(getActivity().getApplicationContext());

        // action handlers for logging buttons
        //Stop button
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), "User has stopped logging!", Snackbar.LENGTH_SHORT).show();
                Log.d(LOG, "User has stopped logging!");

                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);
                btn_reset.setEnabled(false);
            }
        });

        //Start button
        btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Snackbar.make(getActivity().findViewById(android.R.id.content), "User has started logging!", Snackbar.LENGTH_SHORT).show();
                    Log.d(LOG, "User has started logging!");

                    logger = new Logger(getActivity());

                    btn_start.setEnabled(false);
                    btn_stop.setEnabled(true);
                    btn_reset.setEnabled(true);

                }
            });

        //Reset button
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), "User has reset the log!", Snackbar.LENGTH_SHORT).show();
                Log.d(LOG, "User has reset logging!");
            }
        });

        //Log switch
        switch_log.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    gnss_retriever.requestData();
                } else {
                    gnss_retriever.stopGettingData();
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