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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ai2s_lab.gnss_dr.MainActivity;
import com.ai2s_lab.gnss_dr.R;
import com.ai2s_lab.gnss_dr.databinding.FragmentLogBinding;
import com.ai2s_lab.gnss_dr.gnss.FusedRetriever;
import com.ai2s_lab.gnss_dr.gnss.GnssRetriever;
import com.ai2s_lab.gnss_dr.io.Logger;
import com.ai2s_lab.gnss_dr.model.Satellite;
import com.ai2s_lab.gnss_dr.util.LogListAdapter;
import com.ai2s_lab.gnss_dr.util.Settings;
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class LogFragment extends Fragment {

    // constants
    private LogViewModel logViewModel;
    private FragmentLogBinding binding;
    private final String LOG = "LOG";
    public boolean isLogging;

    // GPS providers
    private GnssRetriever gnss_retriever;
    private FusedRetriever fused_retriever;

    // UI elements
    private TextView tv_log_title;
    private TextView tv_subtitle;

    private TextView tv_lat;
    private TextView tv_long;
    private TextView tv_speed;
    private TextView tv_height;
    private TextView tv_num_sat;
    private TextView tv_bearing;

    private Button btn_start;
    private Button btn_reset;
    private Button btn_stop;
    private Switch switch_gnss;
    private Logger logger;

    // UI elements + data for listview
    private ListView listView;
    private ArrayList<Satellite> satellites;
    private LogListAdapter logListAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // init view and inflate
        logViewModel = new ViewModelProvider(this).get(LogViewModel.class);
        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //initialise retrievers
        gnss_retriever = new GnssRetriever(getActivity().getApplicationContext(), this);
        fused_retriever = new FusedRetriever(this);

        // initialize UI components
        tv_log_title = binding.textLogTitle;
        tv_subtitle = binding.textLogFile;

        btn_reset = binding.btnLogReset;
        btn_start = binding.btnLogStart;
        btn_stop = binding.btnLogStop;
        switch_gnss = binding.switchLogTrack;

        tv_lat = binding.textLogLatValue;
        tv_long = binding.textLogLongValue;
        tv_speed = binding.textLogSpeedValue;
        tv_height = binding.textLogHeightValue;
        tv_num_sat = binding.textLogNumValue;
        tv_bearing = binding.textLogBearingValue;

        // set default values
        tv_subtitle.setText("No Logfile Created!");

        if(Settings.getGpsChoice() == 1) {
            tv_log_title.setText("Using FusedLocationProvider");
            switch_gnss.setClickable(true);
            visibleUI();
        }
        else if(Settings.getGpsChoice() == 2){
            switch_gnss.setClickable(true);
            tv_log_title.setText("Using GNSS");
            visibleUI();
        }
        else{
            invisibleUI();
        }

        // states for logging buttons
        btn_stop.setEnabled(false);
        btn_reset.setEnabled(false);
        switch_gnss.setChecked(false);

        // initialize switch
        if(Settings.getGPS()){
            switch_gnss.setChecked(true);
            switch_gnss.setText("GPS On");
            gnss_retriever.requestData();
        } else{
            gnss_retriever.stopGettingData();
            switch_gnss.setText("GPS OFF");
            switch_gnss.setChecked(false);
            resetList();
            resetUI();
        }

        // adapter for listview
        this.satellites = new ArrayList<>();
        logListAdapter = new LogListAdapter(this.getContext(), satellites);
        listView = binding.listLog;
        listView.setAdapter(logListAdapter);

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
                isLogging = false;
                tv_subtitle.setText("No Logfile Created!");

            }
        });

        //Start button
        btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Snackbar.make(getActivity().findViewById(android.R.id.content), "User has started logging!", Snackbar.LENGTH_SHORT).show();
                    Log.d(LOG, "User has started logging!");

                    if(switch_gnss.isChecked()){
                        logger = new Logger(getActivity());

                        tv_subtitle.setText("Logging on \'" + logger.getFileName() + "\'");
                        btn_start.setEnabled(false);
                        btn_stop.setEnabled(true);
                        btn_reset.setEnabled(true);
                        isLogging = true;
                    } else {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "GNSS Is Off", Snackbar.LENGTH_SHORT).show();

                    }


                }
            });

        //Reset button
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), "User has reset the log!", Snackbar.LENGTH_SHORT).show();
                Log.d(LOG, "User has reset logging!");
                logger.resetFile();
            }
        });

        //Log switch
        switch_gnss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if(Settings.getGpsChoice() == 2)
                        gnss_retriever.requestData();
                    else if(Settings.getGpsChoice() == 1)
                        fused_retriever.requestData();

                    switch_gnss.setText("GPS On");
                    Settings.toggleGPS();
                } else {
                    if(Settings.getGpsChoice() == 2)
                        gnss_retriever.stopGettingData();
                    else if(Settings.getGpsChoice() == 1)
                        gnss_retriever.stopGettingData();

                    switch_gnss.setText("GPS OFF");
                    resetList();
                    resetUI();
                    Settings.toggleGPS();
                }
            }
        });

        return root;
    }
//    public void updateUIComponents() {
//        tv_lat.setText();
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateChart(double lat, double lon, double alt, double bearing, double speed){
        tv_lat.setText(Double.toString(lat));
        tv_long.setText(Double.toString(lon));
        tv_height.setText(Double.toString(alt));
        tv_bearing.setText(Double.toString(bearing));
        tv_speed.setText(Double.toString(speed));
    }

    public void updateSatNum(int satNum){
        tv_num_sat.setText(Integer.toString(satNum));
    }
    public void updateList(ArrayList<Satellite> satellites){

        this.satellites = satellites;
        logListAdapter = new LogListAdapter(getActivity(), satellites);
        listView = binding.listLog;
        listView.setAdapter(logListAdapter);
    }

    public Logger getLogger() { return this.logger; }

    public void resetList(){
        this.satellites = new ArrayList<>();
        logListAdapter = new LogListAdapter(getActivity(), satellites);
        listView = binding.listLog;
        listView.setAdapter(logListAdapter);
    }

    public void resetUI(){
        tv_lat.setText("N/A");
        tv_long.setText("N/A");
        tv_speed.setText("N/A");
        tv_height.setText("N/A");
        tv_num_sat.setText("N/A");
        tv_bearing.setText("N/A");
    }

    public void invisibleUI(){
        tv_log_title.setVisibility(View.INVISIBLE);
        tv_subtitle.setVisibility(View.INVISIBLE);
        switch_gnss.setVisibility(View.INVISIBLE);
        CardView log_info =  binding.logInfo;
        log_info.setVisibility(View.INVISIBLE);
        CardView log_sats = binding.logSats;
        log_sats.setVisibility(View.INVISIBLE);

        LinearLayout log_btns = binding.logButtonLayout;
        log_btns.setVisibility(View.INVISIBLE);

        TextView tv_placeholder = binding.tvPlaceholder;
        tv_placeholder.setText("Please select a GPS provider first!");
    }

    public void visibleUI(){
        tv_log_title.setVisibility(View.VISIBLE);
        tv_subtitle.setVisibility(View.VISIBLE);
        switch_gnss.setVisibility(View.VISIBLE);
        CardView log_info =  binding.logInfo;
        log_info.setVisibility(View.VISIBLE);
        CardView log_sats = binding.logSats;
        log_sats.setVisibility(View.VISIBLE);

        LinearLayout log_btns = binding.logButtonLayout;
        log_btns.setVisibility(View.VISIBLE);

        TextView tv_placeholder = binding.tvPlaceholder;
        tv_placeholder.setVisibility(View.INVISIBLE);
    }
}