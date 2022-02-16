package com.ai2s_lab.gnss_dr.gnss;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ai2s_lab.gnss_dr.util.Settings;
import com.ai2s_lab.gnss_dr.model.Satellite;
import com.ai2s_lab.gnss_dr.ui.log.LogFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class GnssRetriever {
    private static final String TAG = "GNSSRetriever";

    private int satCount;
    private boolean canUpdateUI;

    //init location manager
    private final LocationManager mLocationManager;
    private LogFragment logFragment;

    //Initial frequency for logging GNSS signals
    private int logFrequency = Settings.getUpdateFrequency();

    public GnssRetriever(Context context, LogFragment logFragment) {
        this.mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.logFragment = logFragment;
        satCount = 0;
        canUpdateUI = false;
    }

    //Listener for Location data
    private final LocationListener mLocationListener = new LocationListener() {
        private static final String TAG = "LocationListener";

        // On location change, collect all required GNSS data
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            double altitude = -1;
            double bearing = -1;
            double speed = -1;
            double horizontalAccuracy = -1;
            double verticalAccuracy = -1;
            double speedAccuracy = -1;
            long timeMilliLong = location.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String timeAsString = formatter.format(timeMilliLong);

            location.getExtras();
            if(location.hasAltitude())
                altitude = location.getAltitude();

            if(location.hasBearing())
                bearing = location.getBearing();

            if(location.hasSpeed())
                speed = location.getSpeed();

            if(location.hasAccuracy()) {
                horizontalAccuracy = location.getAccuracy();
            }

            if(location.hasVerticalAccuracy())
                verticalAccuracy = location.getVerticalAccuracyMeters();

            if(location.hasSpeedAccuracy())
                speedAccuracy = location.getSpeedAccuracyMetersPerSecond();

            //Only update UI when UI is visible
            if(logFragment.isVisible()){
                logFragment.updateChart(latitude, longitude, altitude, bearing, speed, horizontalAccuracy, verticalAccuracy, speedAccuracy);

                if(location.hasAltitude() && logFragment.getSatCount() >= 4)
                    logFragment.updateFixStatus("3D Fix");
                else if(logFragment.getSatCount() >= 3){
                    logFragment.updateFixStatus("2D Fix");
                } else {
                    logFragment.updateFixStatus("No Fix");
                    logFragment.resetUI();
                }
            }

            String provider = location.getProvider();

            // If log fragment is logging, then update data.
            if(logFragment.isLogging){
                Log.d(TAG, "time logging: " + timeAsString);
                String [] temp = {Double.toString(latitude), Double.toString(longitude), Double.toString(speed), Double.toString(altitude), "" , Double.toString(bearing), timeAsString};
                logFragment.getLogger().addData(temp);
                logFragment.updateSubtitle(logFragment.getLogger().getDataCount());
            }

            Log.d(TAG, "lat: " + latitude
                    + " long: " + longitude
                    + " alt: " + altitude
                    + " acc: " + horizontalAccuracy
                    + " bearing: " + bearing
                    + " speed: " + speed);
            Log.d(TAG, "provider: " + provider);

            if(logFragment.getMapShown()){
                logFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), logFragment.getZoom()));
            }
        }
    };

    //Listener for GNSS data (satellite info)
    private final GnssStatus.Callback gnssStatusListener = new GnssStatus.Callback() {
        private static final String TAG = "GnssStatusCallback";

        @Override
        public void onFirstFix(int ttffMillis) {
            super.onFirstFix(ttffMillis);
        }

        @Override
        public void onStopped(){
            Log.d(TAG, "GNSS has stopped");
        }
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            int tempSatCount = status.getSatelliteCount();

            ArrayList<Satellite> satellites = new ArrayList<>();

            for (int i = 0; i < tempSatCount; i++) {
                int satType = status.getConstellationType(i);
                String satConstellationName = getConstellationName(satType);
                int satVid = status.getSvid(i);
                boolean satIsUsed = status.usedInFix(i);
                double satElevation = round(status.getElevationDegrees(i), 5);
                double satAzimDegree = round(status.getAzimuthDegrees(i), 5);
                double satCarToNoiseRatio = round(status.getCn0DbHz(i), 5);
                if(satIsUsed){
                    Log.d(TAG, " satellite ID: " + satVid
                            + "  constellation type: " + satConstellationName
                            + " satellite used: " + satIsUsed
                            + " elevation: " + satElevation
                            + " azimuth: " + satAzimDegree
                            + " carrier2noiseR: " + satCarToNoiseRatio);

                    Satellite satellite = new Satellite(satVid, satConstellationName, satIsUsed, satElevation, satAzimDegree, satCarToNoiseRatio);
                    satellites.add(satellite);

                    if(logFragment.isLogging){
                        String [] temp = {"", "", "", "", "", "", Integer.toString(satVid), satConstellationName, Boolean.toString(satIsUsed), Double.toString(satElevation), Double.toString(satAzimDegree), Double.toString(satCarToNoiseRatio)};
                        logFragment.getLogger().addData(temp);
                    }
                }
            }

            satCount = satellites.size();

            if(satCount > 3){
                logFragment.applyGNSS();
            } else {
                if(logFragment.getIsUsingGNSS()){
                    logFragment.applyFused();
                }
            }

            if(logFragment.isVisible() && canUpdateUI){
                logFragment.updateList(satellites);
                logFragment.updateSatNum(satellites.size());
                Log.i(TAG, "satellite count: " + satCount);

                if(logFragment.isLogging){
                    logFragment.updateSubtitle(logFragment.getLogger().getDataCount());

                }
            }

        }
    };


    @SuppressLint("MissingPermission")
    public void requestData() {
        boolean isEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, logFrequency, 0.0f, mLocationListener);
            mLocationManager.registerGnssStatusCallback(gnssStatusListener, null);
        }
    }

    public void stopGettingData() {
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager.unregisterGnssStatusCallback(gnssStatusListener);
    }

    private String getConstellationName(int typeNo) {
        switch(typeNo) {
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
        return logFrequency;
    }

    //set
    //log_frequency
    public void setLogFrequency(int logFreq) {
        logFrequency = logFreq;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public int getSatCount() { return this.satCount; }

    public boolean getCanUpdateUI() { return this.canUpdateUI; }

    public void setCanUpdateUI(boolean value) { this.canUpdateUI = value; }

}
