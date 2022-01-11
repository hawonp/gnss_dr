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
    private FileWriter file_writer;
    private CSVWriter csv_writer;
    private String [] first_line;

//    private Context context;
    private Activity activity;


    public Logger(Activity activity){

        this.activity = activity;
        file_name = "gnss_log_" + getCurrentTime() + ".csv";
        base_dir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        file_path = base_dir + File.separator +  file_name;

        file = new File(file_path);

        first_line = new String[]{"Lat", "Long", "Speed", "Height", "NumSats", "Bearing", "Sat_ID", "Sat_Type", "Sat_Is_Used", "Sat_Elev", "Sat_Azim", "Sat_CNO"};

        writeALine(first_line);

        Log.d(TAG, file_name + " created");
        Snackbar.make(activity.findViewById(android.R.id.content), file_name + " created", Snackbar.LENGTH_SHORT).show();

    }

    public String getFileName() { return file_name; }

    // TODO Once GNSS data is known
    public void logData(String input){
        Log.d(TAG, input);
    }

    private String getCurrentTime(){
        String current_date = new SimpleDateFormat("yy_MM_dd", Locale.getDefault()).format(new Date());
        String current_time = new SimpleDateFormat("HH_mm_ss", Locale.getDefault()).format(new Date());
        return current_date + "_" + current_time ;
    }

    public void resetFile(){
        try {
            file.createNewFile();
            csv_writer = new CSVWriter(new FileWriter(file_path));

            csv_writer.writeNext(first_line);
            csv_writer.close();

        } catch (IOException e){
            Snackbar.make(activity.findViewById(android.R.id.content), "Could not reset log file", Snackbar.LENGTH_SHORT).show();

        }

    }

    public void writeALine(String [] text) {
        try {
            if (file.exists() && !file.isDirectory()) {
                file_writer = new FileWriter(file_path, true);
                csv_writer = new CSVWriter(file_writer);
            } else {
                file.createNewFile();
                csv_writer = new CSVWriter(new FileWriter(file_path));
            }

            csv_writer.writeNext(text);
            csv_writer.close();

        } catch (IOException e) {
            Snackbar.make(activity.findViewById(android.R.id.content), "Could not create a log file!", Snackbar.LENGTH_SHORT).show();

        }
    }







}
