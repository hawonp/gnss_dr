package com.ai2s_lab.gnss_dr.gnss;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.ai2s_lab.gnss_dr.ui.log.LogFragment;
import com.ai2s_lab.gnss_dr.util.Settings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.util.concurrent.Executor;

public class FusedRetriever2 {

    // constants
    private LocationCallback locationCallback;
    private Location lastKnownLocation;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int DEFAULT_INTERVAL = Settings.getUpdateFrequency();
    private static final int POWER_INTERVAL = 0;

    private Context context;
    public FusedRetriever2(Context context ){
        this.context = context;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        // init location request
        locationRequest = LocationRequest.create()
                .setInterval(DEFAULT_INTERVAL)
                .setFastestInterval(POWER_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // event triggered for location update
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUI(locationResult.getLastLocation());
            }
        };
    }

    public void stopGettingData(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    public void requestData(){
//        LocationServices.getFusedLocationProviderClient(logFragment.getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
//        updateGPS();
    }

    private void updateUI(Location location){
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        double altitude = -1;
        double bearing = -1;
        double speed = -1;
        double horizontal_accuracy = -1;
        double vertical_accuracy = -1;
        double speed_accuracy = -1;

        if(location.hasAltitude())
            altitude = location.getAltitude();

        if(location.hasBearing())
            bearing = location.getBearing();

        if(location.hasSpeed())
            speed = location.getSpeed();

        if(location.hasAccuracy())
            horizontal_accuracy = location.getAccuracy();

        if(location.hasVerticalAccuracy())
            vertical_accuracy = location.getVerticalAccuracyMeters();

        if(location.hasSpeedAccuracy())
            speed_accuracy = location.getSpeedAccuracyMetersPerSecond();

//        if(logFragment.isVisible()){
//            logFragment.updateChart(latitude, longitude, altitude, bearing, speed, horizontal_accuracy, vertical_accuracy, speed_accuracy);
//        }

//        TextView tv_lat =
        Log.d("FUSED2", String.valueOf(longitude));
//        Log.d("FUSED", String.valueOf(location.getElapsedRealtimeNanos()));

    }

}
