package com.ai2s_lab.gnss_dr.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionSupport {

    private Context context;
    private Activity activity;

    private String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private List permission_list;
    private final int MULTIPLE_PERMISSIONS = 1023;

    public PermissionSupport(Activity activity, Context context){
        this.activity = activity;
        this.context = context;
    }

    public boolean arePermissionsEnabled(){
        int result;
        permission_list = new ArrayList<>();

        for(String pm : permissions){
            if(ActivityCompat.checkSelfPermission(context, pm) != PackageManager.PERMISSION_GRANTED)
                permission_list.add(pm);
        }

        if(permission_list.size() > 0 )
            return false;

        return true;
    }

    public void requestMultiplePermissions(){
        ActivityCompat.requestPermissions(activity, (String[]) permission_list.toArray(new String[permission_list.size()]), MULTIPLE_PERMISSIONS);
    }

    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == MULTIPLE_PERMISSIONS && (grantResults.length > 0)){
            for(int i = 0; i < grantResults.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                   if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])){
                       requestMultiplePermissions();
                   }
                return false;
            }
        }
        return true;
    }

}
