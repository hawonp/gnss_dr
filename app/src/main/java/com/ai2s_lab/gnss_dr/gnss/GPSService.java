package com.ai2s_lab.gnss_dr.gnss;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ai2s_lab.gnss_dr.MainActivity;
import com.ai2s_lab.gnss_dr.ui.log.LogFragment;

public class GPSService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private GnssRetriever gnssRetriever;
    private FusedRetriever2 fusedRetriever2;
    private FusedRetriever fusedRetriever;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        input = "This is me testing this out";
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();

        Log.d("Service", "starting service");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        fusedRetriever2.stopGettingData();
        super.onDestroy();
    }

    @Override
    public void onCreate(){
        super.onCreate();


        fusedRetriever2 = new FusedRetriever2(this.getApplicationContext());
        fusedRetriever2.requestData();
    }

    private void createNotificationChannel(){
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }


}
