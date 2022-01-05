package com.ai2s_lab.gnss_dr.ui.log;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ai2s_lab.gnss_dr.databinding.FragmentLogBinding;

public class LogFragment extends Fragment {

    private LogViewModel logViewModel;
    private FragmentLogBinding binding;
    private Button btn_start;
    private Button btn_reset;
    private Button btn_stop;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        logViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // import UI items
        final TextView text_title = binding.textLogTitle;

        btn_reset = binding.btnLogReset;
        btn_start = binding.btnLogStart;
        btn_stop = binding.btnLogStop;

        logViewModel.getTitle().observe(getViewLifecycleOwner(), s -> {
            text_title.setText(s);
        });




        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}