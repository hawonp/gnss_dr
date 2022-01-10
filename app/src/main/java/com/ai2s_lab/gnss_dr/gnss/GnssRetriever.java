package com.ai2s_lab.gnss_dr.gnss;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class GnssRetriever {
    private static final String TAG = "GNSSRetriever";

    private int log_frequency = 100;
    private final LocationManager my_location_manager;

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

            Log.i(TAG, "satellite count: " + satCount);
            for (int i = 0; i < satCount; i++) {
                int sat_type = status.getConstellationType(i);
                boolean sat_is_used = status.usedInFix(i);
                double sat_elevation = status.getElevationDegrees(i);
                double sat_azim_degree = status.getAzimuthDegrees(i);
                double sat_car_t_noise_r = status.getCn0DbHz(i);

                Log.d(TAG, "  constellation type: " + sat_type
                        + " satellite used: " + sat_is_used
                        + " elevation: " + sat_elevation
                        + " azimuth: " + sat_azim_degree
                        + " carrier2noiseR: " + sat_car_t_noise_r);
            }
        }
    };

    public GnssRetriever(Context context) {
        this.my_location_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public void requestData() {
        boolean isEnabled = my_location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isEnabled) {
            my_location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, log_frequency, 0.0f, my_location_listener);
            my_location_manager.registerGnssStatusCallback(gnss_status_listener, null);
        }
    }

    public void stopGettingData() {
        my_location_manager.removeUpdates(my_location_listener);
        my_location_manager.unregisterGnssStatusCallback(gnss_status_listener);
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
