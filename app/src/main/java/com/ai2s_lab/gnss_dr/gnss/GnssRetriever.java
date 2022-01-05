package com.ai2s_lab.gnss_dr.gnss;

import android.content.Context;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.util.Log;

import androidx.annotation.NonNull;

public class GnssRetriever {
    private static final String TAG = "GNSSRetriever";

    private boolean my_log_data = false;
    private final LocationManager my_location_manager;

    //Listener for Location data
    private final LocationListener my_location_listener = new LocationListener() {
        private static final String TAG = "LocationListener";

        @Override
        public void onLocationChanged(@NonNull Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();


            Log.i(TAG, "lat: " + latitude);
            Log.i(TAG, "long: " + longitude);
            Log.i(TAG, "alt: " + altitude);
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
        }
    };

    public GnssRetriever(Context context) {
        this.my_location_manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void printGnssData() {
        boolean isEnabled = my_location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isEnabled) {
            my_location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0.0f, my_location_listener);
        }

//        my_location_manager.registerGnssMeasurementsCallback(gnss_status_listener);

    }

}
