package com.ai2s_lab.gnss_dr.gnss;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Looper;


import androidx.annotation.NonNull;

import com.ai2s_lab.gnss_dr.ui.log.LogFragment;
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

public class FusedRetriever {

    // constants
    private LocationCallback locationCallback;
    private Location lastKnownLocation;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int DEFAULT_INTERVAL = 5000;
    private static final int POWER_INTERVAL = 0;

    private LogFragment logFragment;

    public FusedRetriever(LogFragment logFragment){
        this.logFragment = logFragment;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(logFragment.getContext());

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
        DecimalFormat five_points = new DecimalFormat("#.#####");
        DecimalFormat one_point = new DecimalFormat("#.#");

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        double horizontal_accuracy = location.getAccuracy();
        double bearing = location.getBearing();
        double speed = location.getSpeed();

        if(logFragment.isVisible()){
            logFragment.updateChart(latitude, longitude, altitude, bearing, speed);
        }

    }

//    @SuppressLint("MissingPermission")
//    private void updateGPS(){
//        //user granted permission
//        fusedLocationProviderClient.getLastLocation().addOnSuccessListener((Executor) this, new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                //update from locations
//                updateUI(location);
//                lastKnownLocation = location;
//            }
//        });
//
//    }

}
