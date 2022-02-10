package com.ai2s_lab.gnss_dr.ui.log;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.cardview.widget.CardView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ai2s_lab.gnss_dr.R;
import com.ai2s_lab.gnss_dr.databinding.FragmentLogBinding;
import com.ai2s_lab.gnss_dr.dropbox.DropboxClient;
import com.ai2s_lab.gnss_dr.gnss.FusedRetriever;
import com.ai2s_lab.gnss_dr.gnss.GnssRetriever;
import com.ai2s_lab.gnss_dr.gnss.GnssService;
import com.ai2s_lab.gnss_dr.io.Logger;
import com.ai2s_lab.gnss_dr.model.Satellite;
import com.ai2s_lab.gnss_dr.util.LogListAdapter;
import com.ai2s_lab.gnss_dr.util.Settings;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class LogFragment extends Fragment   {

    // constants
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
    private TextView tv_fix_status;

    private Button btn_start;
    private Button btn_reset;
    private Button btn_save;
    private Button btn_map;
    private Switch switch_gnss;

    private CardView log_info;
    private CardView log_sats;
    private LinearLayout log_btns;

    private TextView tv_update_freq;

    // UI elements + data for listview
    private ListView listView;
    private ArrayList<Satellite> satellites;
    private LogListAdapter logListAdapter;

    // Logger
    private Logger logger;

    // alert dialog
    private AlertDialog.Builder builder;

    // constants for map
    private static final float DEFAULT_ZOOM = 17;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private GoogleMap map;
    private MapView mapView;
    private BottomSheetDialog bottomSheetDialog;
    private boolean dialogShown;

    // dropbox client
    private DropboxClient dropboxClient;

    // Service constant
    private boolean serviceOn;

    private boolean isUsingGNSS;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // init view and inflate
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
        btn_map = binding.btnMap;
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
        tv_fix_status = binding.tvFixStatus;

        log_info =  binding.logInfo;
        log_sats = binding.logSats;
        log_btns = binding.logButtonLayout;

        tv_update_freq = binding.tvUpdateFrequency;

        // initial states for logging buttons
        btn_save.setEnabled(false);
        btn_reset.setEnabled(false);
        switch_gnss.setChecked(false);
        tv_subtitle.setText("Not Logging");
        tv_log_title.setText("GPS is turned off!");

        // apply current settings
        applyCurrentSettings();

        // adapter for listview
        this.satellites = new ArrayList<>();
        logListAdapter = new LogListAdapter(this.getContext(), satellites);
        listView = binding.listLog;
        listView.setAdapter(logListAdapter);

        // service constants
        serviceOn = false;
        isUsingGNSS = true;

        // create an Alert dialog
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Save Data");
        builder.setMessage("Are you sure you want to write the log date to a CSV file?");
        builder.setCancelable(false);

        builder.setPositiveButton("Save File", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logger.saveDataToFile();
                dropboxClient = new DropboxClient();
                if(dropboxClient.uploadFile(logger.getFilePath(), logger.getFileName())){
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Uploaded to Dropbox", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Upload failed", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Stop Logging", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logger.resetData();
                dialogInterface.cancel();
            }
        });

        // initialize google map
//        SupportMapFragment mapFragment = (SupportMapFragment) getParentFragmentManager().findFragmentById(R.id.map);
//        assert mapFragment != null;
//        mapFragment.getMapAsync(this);
        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);

        mapView = bottomSheetDialog.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getContext());
        } catch (Exception e){
            e.printStackTrace();
        }
        mapView.onResume();
        dialogShown = false;


        // action handlers for logging buttons
        //save button
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(getActivity().findViewById(android.R.id.content), "You have stopped logging!", Snackbar.LENGTH_SHORT).show();
                btn_start.setEnabled(true);
                btn_save.setEnabled(false);
                btn_reset.setEnabled(false);
                isLogging = false;
                tv_subtitle.setText("Not Logging");

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //Start button
        btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Snackbar.make(getActivity().findViewById(android.R.id.content), "You have started logging!", Snackbar.LENGTH_SHORT).show();

                    if(switch_gnss.isChecked()){
                        logger = new Logger(getActivity());

                        tv_subtitle.setText("Started Logging GPS Data");
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
                Snackbar.make(getActivity().findViewById(android.R.id.content), "You have reset the logged data", Snackbar.LENGTH_SHORT).show();
                logger.resetData();
            }
        });

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogShown = true;
                showMap(savedInstanceState);
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
            tv_vertical_accuracy.setText(one_point.format(vertical_accuracy) + " m");
        else
            tv_vertical_accuracy.setText("N/A");

        if(speed_accuracy != -1)
            tv_speed_accuracy.setText(one_point.format(speed_accuracy) + "%");
        else
            tv_speed_accuracy.setText("N/A");
    }

    public void updateFixStatus(String status){ tv_fix_status.setText(status); }
    public void updateSatNum(int satNum){
        tv_num_sat.setText(Integer.toString(satNum));
    }

    public int getSatCount() {
        return Integer.parseInt((String) tv_num_sat.getText());
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

    public void resetUI(){
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

    public void applyGNSS(){
        tv_log_title.setText("Using GNSS");
        gnss_retriever.setCanUpdateUI(true);
        fused_retriever.setCanUpdateUI(false);
        gnss_retriever.requestData();
        fused_retriever.stopGettingData();
        tv_fix_status.setVisibility(View.VISIBLE);
        isUsingGNSS = true;
    }

    public void applyFused(){
//        resetUI();
        tv_log_title.setText("Using FusedLocationProvider");
        log_sats.setVisibility(View.INVISIBLE);
        tv_num_sat.setText("N/A");
        tv_fix_status.setVisibility(View.INVISIBLE);
        gnss_retriever.setCanUpdateUI(false);
        fused_retriever.setCanUpdateUI(true);
        fused_retriever.requestData();
        isUsingGNSS = false;
    }


    private void applyCurrentSettings(){

        // GPS On
        if(Settings.getGPS()){
            tv_update_freq.setText("Update Every " + Settings.getUpdateFrequency() + "ms");
            switch_gnss.setText("GPS On");
            switch_gnss.setChecked(true);
            btn_map.setEnabled(true);
//            startService();
//            applyGNSS(true);
//            if(isUsingGNSS)
//            else
//                fused_retriever.requestData();
            gnss_retriever.requestData();

            tv_update_freq.setVisibility(View.VISIBLE);
        }
        // GPS Off
        else{
            tv_update_freq.setVisibility(View.INVISIBLE);
            tv_log_title.setText("GPS is turned off!");
            switch_gnss.setText("GPS Off");
            tv_fix_status.setVisibility(View.INVISIBLE);
            switch_gnss.setChecked(false);
            btn_map.setEnabled(false);
            resetList();
            resetUI();
//            stopService();
            fused_retriever.stopGettingData();
            gnss_retriever.stopGettingData();
            isUsingGNSS = true;

        }
    }

    private void startService() {
        serviceOn = true;
        Intent serviceIntent = new Intent(getActivity(), GnssService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(getActivity(), serviceIntent);
    }

    private void stopService(){
//        serviceIntent = new Intent(this.getActivity(), GnssService.class);
//        if(serviceOn == true)
//            stopService(serviceIntent);
//        serviceOn = false;
        getContext().stopService(new Intent(getActivity(), GnssService.class));
    }

    private void showMap(Bundle savedInstanceState){
        bottomSheetDialog.show();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                map = googleMap;
                map.setMyLocationEnabled(true);
//                // For dropping a marker at a point on the Map
//                LatLng sydney = new LatLng(-34, 151);
//                map.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));
//
//                // For zooming automatically to the location of the marker
//                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
//                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

//                LatLng start = new LatLng(Settings.getLatitude(), Settings.getLongtitude());
//                map.addMarker(new MarkerOptions().position(start).title("GPS").snippet("Marker"));
//                CameraPosition cameraPosition = new CameraPosition.Builder().target(start).zoom(DEFAULT_ZOOM).build();
//                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public boolean getMapShown() { return this.dialogShown; }

    public float getZoom() { return DEFAULT_ZOOM; }
    public GoogleMap getMap() { return this.map; }

    public void updateSubtitle(int count){
        String temp = "Logged " + count + " lines";
        tv_subtitle.setText(temp);
    }

    public boolean getIsUsingGNSS(){ return this.isUsingGNSS; }
}