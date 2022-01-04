package com.ai2s_lab.gnss_dr;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ai2s_lab.gnss_dr.databinding.ActivityBottomNavBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityBottomNavBinding binding;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int PERMISSION_FINE_LOCATION = 99;
    private final int LOCATION_REQUEST_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBottomNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        requestPermissions(this);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_log, R.id.navigation_map, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_nav);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSION_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    System.out.println("Permission granted");
                }
        }
    }

    // Check for permissions and request permissions
    private boolean isPermissionGranted() {
        System.out.println("Checking for permissions");
        for (String permission: permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission not granted");
                return false;
            }
        }
        System.out.println("Permission granted");
        return true;
    }

    private void requestPermissions(final Activity activ) {
        if (!isPermissionGranted()) {
            System.out.println("Requesting permission");
            ActivityCompat.requestPermissions(activ, permissions, LOCATION_REQUEST_ID);
        }
        else {
            System.out.println("Permission granted");
        }
    }
}