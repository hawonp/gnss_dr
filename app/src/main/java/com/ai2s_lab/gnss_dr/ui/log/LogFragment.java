package com.ai2s_lab.gnss_dr.ui.log;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import androidx.fragment.app.DialogFragment;
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
import java.text.DecimalFormat;
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
    private TextView tv_horizontal_accuracy;
    private TextView tv_vertical_accuracy;
    private TextView tv_speed_accuracy;

    private Button btn_start;
    private Button btn_reset;
    private Button btn_save;
    private Switch switch_gnss;
    private Logger logger;

    private CardView log_info;
    private CardView log_sats;
    private LinearLayout log_btns;
    private TextView tv_placeholder;

    private TextView tv_update_freq;

    // UI elements + data for listview
    private ListView listView;
    private ArrayList<Satellite> satellites;
    private LogListAdapter logListAdapter;


    // alert dialog
    private AlertDialog.Builder builder;

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
        btn_save = binding.btnLogSave;
        switch_gnss = binding.switchLogTrack;

        tv_lat = binding.textLogLatValue;
        tv_long = binding.textLogLongValue;
        tv_speed = binding.textLogSpeedValue;
        tv_height = binding.textLogHeightValue;
        tv_num_sat = binding.textLogNumValue;
        tv_bearing = binding.textLogBearingValue;
        tv_horizontal_accuracy = binding.textLogXValue;
        tv_vertical_accuracy = binding.textLogYValue;
        tv_speed_accuracy = binding.textLogSpeedAccuracyValue;

        log_info =  binding.logInfo;
        log_sats = binding.logSats;
        log_btns = binding.logButtonLayout;
        tv_placeholder = binding.tvPlaceholder;

        tv_update_freq = binding.tvUpdateFrequency;

        // initial states for logging buttons
        btn_save.setEnabled(false);
        btn_reset.setEnabled(false);
        switch_gnss.setChecked(false);
        tv_subtitle.setText("No Logfile Created!");

        // apply current settings
        applyCurrentSettings();

        // adapter for listview
        this.satellites = new ArrayList<>();
        logListAdapter = new LogListAdapter(this.getContext(), satellites);
        listView = binding.listLog;
        listView.setAdapter(logListAdapter);


        // create an Alert dialog
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Save Data");
        builder.setMessage("Are you sure you want to write the log date to a CSV file?");
        builder.setCancelable(false);

        builder.setPositiveButton("Save File", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logger.saveDataToFile();
            }
        });

        builder.setNegativeButton("Stop Logging", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logger.deleteData();
                dialogInterface.cancel();
            }
        });

        // action handlers for logging buttons
        //save button
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), "User has stopped logging!", Snackbar.LENGTH_SHORT).show();
                Log.d(LOG, "User has stopped logging!");
                btn_start.setEnabled(true);
                btn_save.setEnabled(false);
                btn_reset.setEnabled(false);
                isLogging = false;
                tv_subtitle.setText("No Logfile Created!");

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //Start button
        btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Snackbar.make(getActivity().findViewById(android.R.id.content), "User has started logging!", Snackbar.LENGTH_SHORT).show();

                    if(switch_gnss.isChecked()){
                        logger = new Logger(getActivity());

                        tv_subtitle.setText("Logging on \'" + logger.getFileName() + "\'");
                        btn_start.setEnabled(false);
                        btn_save.setEnabled(true);
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

        //Log switch listener
        switch_gnss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Settings.toggleGPS();
                applyCurrentSettings();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateChart(double lat, double lon, double alt, double bearing, double speed, double horizontal_accuracy, double vertical_accuracy, double speed_accuracy){
        DecimalFormat five_points = new DecimalFormat("#.#####");
        DecimalFormat one_point = new DecimalFormat("#.#");

        tv_lat.setText(five_points.format(lat));
        tv_long.setText(five_points.format(lon));

        if(alt != -1)
            tv_height.setText(five_points.format(alt) + " m");
        else
            tv_height.setText("N/A");

        if(bearing != -1)
            tv_bearing.setText(five_points.format(bearing));
        else
            tv_bearing.setText("N/A");

        if(speed != -1 && speed_accuracy != -1)
            tv_speed.setText(five_points.format(speed) + " m/s");
        else
            tv_speed.setText("N/A");

        if(horizontal_accuracy != -1)
            tv_horizontal_accuracy.setText(one_point.format(horizontal_accuracy) + "%");
        else
            tv_horizontal_accuracy.setText("N/A");

        if(vertical_accuracy != -1)
            tv_vertical_accuracy.setText(one_point.format(vertical_accuracy) + "%");
        else
            tv_vertical_accuracy.setText("N/A");

        if(speed_accuracy != -1)
            tv_speed_accuracy.setText(one_point.format(speed_accuracy) + "%");
        else
            tv_speed_accuracy.setText("N/A");
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

    private void resetList(){
        this.satellites = new ArrayList<>();
        logListAdapter = new LogListAdapter(getActivity(), satellites);
        listView = binding.listLog;
        listView.setAdapter(logListAdapter);
    }

    private void resetUI(){
        tv_lat.setText("N/A");
        tv_long.setText("N/A");
        tv_speed.setText("N/A");
        tv_height.setText("N/A");
        tv_num_sat.setText("N/A");
        tv_bearing.setText("N/A");
        tv_horizontal_accuracy.setText("N/A");
        tv_vertical_accuracy.setText("N/A");
        tv_speed_accuracy.setText("N/A");
    }

    private void invisibleUI(){
        tv_log_title.setVisibility(View.INVISIBLE);
        tv_subtitle.setVisibility(View.INVISIBLE);
        switch_gnss.setVisibility(View.INVISIBLE);
        log_info.setVisibility(View.INVISIBLE);
        log_sats.setVisibility(View.INVISIBLE);
        log_btns.setVisibility(View.INVISIBLE);
        tv_placeholder.setText("Please select a GPS provider first!");
    }

    private void visibleUI(){
        tv_log_title.setVisibility(View.VISIBLE);
        tv_subtitle.setVisibility(View.VISIBLE);
        switch_gnss.setVisibility(View.VISIBLE);
        log_info.setVisibility(View.VISIBLE);
        log_sats.setVisibility(View.VISIBLE);
        log_btns.setVisibility(View.VISIBLE);
        tv_placeholder.setVisibility(View.INVISIBLE);
    }

    private void applyGNSS(boolean gpsUsed){
        tv_log_title.setText("Using GNSS");

        if(gpsUsed){
            gnss_retriever.requestData();
            fused_retriever.stopGettingData();
        } else {
            gnss_retriever.stopGettingData();
        }
    }

    private void applyFused(boolean gpsUsed){
        tv_log_title.setText("Using FusedLocationProvider");
        log_sats.setVisibility(View.INVISIBLE);
//        log_btns.setVisibility(View.INVISIBLE);
        tv_num_sat.setText("N/A");

        if(gpsUsed){
            fused_retriever.requestData();
            gnss_retriever.stopGettingData();
        } else {
            fused_retriever.stopGettingData();
        }

    }

    private void applyCurrentSettings(){
        // FusedLocationProvider
        if(Settings.getGpsChoice() == 1){
            visibleUI();
            applyFused(Settings.getGPS());
        }
        // GNSS Provider
        else if(Settings.getGpsChoice() == 2){
            visibleUI();
            applyGNSS(Settings.getGPS());
        }
        // No Provider Selected
        else {
            invisibleUI();
        }

        // GPS On
        if(Settings.getGPS()){
            tv_update_freq.setText("Update Every " + Settings.getUpdateFrequency() + "ms");
            switch_gnss.setText("GPS On");
            switch_gnss.setChecked(true);
        }
        // GPS Off
        else{
            tv_update_freq.setText("GPS is turned off");
            switch_gnss.setText("GPS Off");
            switch_gnss.setChecked(false);

            resetList();
            resetUI();
        }
    }

}