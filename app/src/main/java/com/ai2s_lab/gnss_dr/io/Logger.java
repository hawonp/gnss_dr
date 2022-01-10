package com.ai2s_lab.gnss_dr.io;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.ai2s_lab.gnss_dr.MainActivity;
import com.ai2s_lab.gnss_dr.R;
import com.google.android.material.snackbar.Snackbar;
import com.opencsv.CSVWriter;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class Logger {

    private String TAG = "LOG";
    private String base_dir;
    private String file_name;
    private String file_path;

    private File file;
    private FileWriter fileWriter;
    private CSVWriter csvWriter;

//    private Context context;
    private Activity activity;


    public Logger(Activity activity){

        this.activity = activity;
        file_name = "gnss_log_" + getCurrentTime() + ".csv";
        base_dir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        file_path = base_dir + File.separator +  file_name;

        file = new File(file_path);

        try {
            if(file.exists() && !file.isDirectory()){
                fileWriter = new FileWriter(file_path, true);
                csvWriter = new CSVWriter(fileWriter);
            } else {
                file.createNewFile();
                csvWriter = new CSVWriter(new FileWriter(file_path));
            }

            String [] first_line = {"Lat", "Long", "Speed", "Height", "NumSats", "Bearing"};
            csvWriter.writeNext(first_line);
            csvWriter.close();

        } catch (IOException e){
            Snackbar.make(activity.findViewById(android.R.id.content), "Could not create a log file!", Snackbar.LENGTH_SHORT).show();

        }
        Log.d(TAG, file_name + " created");
        Snackbar.make(activity.findViewById(android.R.id.content), file_name + " created", Snackbar.LENGTH_SHORT).show();

    }

    public String getFileName() { return file_name; }

    // TODO Once GNSS data is known
    public void logData(String input){
        Log.d(TAG, input);
    }

    private String getCurrentTime(){
        String currentDate = new SimpleDateFormat("yy_MM_dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH_mm_ss", Locale.getDefault()).format(new Date());
        return currentDate + "_" + currentTime ;
    }

    public void resetFile(){
        try {
            file.createNewFile();
        } catch (IOException e){
            Snackbar.make(activity.findViewById(android.R.id.content), "Could not reset log file", Snackbar.LENGTH_SHORT).show();

        }

    }







}
