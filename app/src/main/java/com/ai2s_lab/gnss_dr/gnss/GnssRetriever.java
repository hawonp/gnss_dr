package com.ai2s_lab.gnss_dr.gnss;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.ai2s_lab.gnss_dr.R;
import com.ai2s_lab.gnss_dr.databinding.FragmentLogBinding;
import com.ai2s_lab.gnss_dr.model.Satellite;
import com.ai2s_lab.gnss_dr.ui.log.LogFragment;

import java.util.ArrayList;

public class GnssRetriever {
    private static final String TAG = "GNSSRetriever";

    //init location manager
    private final LocationManager my_location_manager;
    private LogFragment logFragment;

    //Initial frequency for logging GNSS signals
    private int log_frequency = 100;

    public GnssRetriever(Context context, LogFragment logFragment) {
        this.my_location_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.logFragment = logFragment;
    }


    //Listener for Location data
    private final LocationListener my_location_listener = new LocationListener() {
        private static final String TAG = "LocationListener";

        @Override
        public void onLocationChanged(@NonNull Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();
            double horizontal_accuracy = location.getAccuracy();
            double bearing = location.getBearing();
            double speed = location.getSpeed();

            String provider = location.getProvider();

            if(logFragment.isVisible()){
                logFragment.updateChart(latitude, longitude, altitude, bearing, speed);
            }
//            first_line = new String[]{"Lat", "Long", "Speed", "Height", "NumSats", "Bearing", "Sat_ID", "Sat_Type", "Sat_Is_Used", "Sat_Elev", "Sat_Azim", "Sat_CNO"};

            if(logFragment.isLogging){
                String [] temp = {Double.toString(latitude), Double.toString(longitude), Double.toString(speed), Double.toString(altitude), "" , Double.toString(bearing)};
                logFragment.getLogger().writeALine(temp);
            }

            Log.d(TAG, "lat: " + latitude
                    + " long: " + longitude
                    + " alt: " + altitude
                    + " acc: " + horizontal_accuracy
                    + " bearing: " + bearing
                    + " speed: " + speed);
            Log.d(TAG, "provider: " + provider);
        }
    };

    //Listener for GNSS data (satellite info)
    private final GnssStatus.Callback gnss_status_listener = new GnssStatus.Callback() {
        private static final String TAG = "GnssStatusCallback";

        @Override
        public void onFirstFix(int ttffMillis) {
            super.onFirstFix(ttffMillis);
        }

        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            int satCount = status.getSatelliteCount();

//            tv_num_sat.setText(satCount);

            Log.i(TAG, "satellite count: " + satCount);

            //            first_line = new String[]{"Lat", "Long", "Speed", "Height", "NumSats", "Bearing", "Sat_ID", "Sat_Type", "Sat_Is_Used", "Sat_Elev", "Sat_Azim", "Sat_CNO"};

            if(logFragment.isLogging){
                String [] temp = {"", "", "", "", Integer.toString(satCount) , ""};
                logFragment.getLogger().writeALine(temp);
            }


            ArrayList<Satellite> satellites = new ArrayList<>();

            for (int i = 0; i < satCount; i++) {
                int sat_type = status.getConstellationType(i);
                String sat_constellation_name = getConstellationName(sat_type);
                int sat_vid = status.getSvid(i);
                boolean sat_is_used = status.usedInFix(i);
                double sat_elevation = status.getElevationDegrees(i);
                double sat_azim_degree = status.getAzimuthDegrees(i);
                double sat_car_t_noise_r = status.getCn0DbHz(i);

                Log.d(TAG, " satellite ID: " + sat_vid
                        + "  constellation type: " + sat_constellation_name
                        + " satellite used: " + sat_is_used
                        + " elevation: " + sat_elevation
                        + " azimuth: " + sat_azim_degree
                        + " carrier2noiseR: " + sat_car_t_noise_r);

                Satellite satellite = new Satellite(sat_vid, sat_constellation_name, sat_is_used, sat_elevation, sat_azim_degree, sat_car_t_noise_r);
                satellites.add(satellite);

                if(logFragment.isLogging){
                    String relativeNum = i+1 + "/" + satCount;
                    String [] temp = {"", "", "", "", relativeNum, "", Integer.toString(sat_vid), sat_constellation_name, Boolean.toString(sat_is_used), Double.toString(sat_elevation), Double.toString(sat_azim_degree), Double.toString(sat_car_t_noise_r)};
                    logFragment.getLogger().writeALine(temp);
                }
            }

            if(logFragment.isVisible()){
                logFragment.updateList(satellites);
                logFragment.updateSatNum(satCount);
            }

        }
    };

    //Listener for Nmea
    private final OnNmeaMessageListener my_nmealistener = new OnNmeaMessageListener() {
        private static final String TAG = "NMEAListener";

        @Override
        public void onNmeaMessage(String s, long l) {
            Log.d(TAG, "Msg: " + s + " timestamp: " + l);
        }
    };


    @SuppressLint("MissingPermission")
    public void requestData() {
        boolean isEnabled = my_location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isEnabled) {
            my_location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, log_frequency, 0.0f, my_location_listener);
            my_location_manager.registerGnssStatusCallback(gnss_status_listener, null);
            my_location_manager.addNmeaListener(my_nmealistener, null);
        }
    }

    public void stopGettingData() {
        my_location_manager.removeUpdates(my_location_listener);
        my_location_manager.unregisterGnssStatusCallback(gnss_status_listener);
        my_location_manager.removeNmeaListener(my_nmealistener);
    }

    private String getConstellationName(int type_no) {
        switch(type_no) {
            case 0:
                return "Unknown";
            case 1:
                return "GPS";
            case 2:
                return "SBAS";
            case 3:
                return "GLONASS";
            case 4:
                return "QZSS";
            case 5:
                return "Beidou";
            case 6:
                return "Galileo";
            case 7:
                return "IRNSS";
        }
        return "ERROR";
    }

    //get
    //log_frequency
    public int getLogFrequency() {
        return log_frequency;
    }

    //set
    //log_frequency
    public void setLogFrequency(int log_freq) {
        log_frequency = log_freq;
    }

}
