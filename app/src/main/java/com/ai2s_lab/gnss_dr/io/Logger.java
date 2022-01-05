package com.ai2s_lab.gnss_dr.io;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.opencsv.CSVWriter;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class Logger {

    private String LOG = "LOG";
    private String base_dir;
    private String file_name;
    private String file_path;

    private File file;
    private FileWriter fileWriter;
    private CSVWriter csvWriter;
//    private Fragment fragment;

    public Logger(){
//        this.fragment = fragment;


//            // initialize OpenCV
//        if(!OpenCVLoader.initDebug()){
//            Log.d("LOG", "Unable to load OpenCV");
//        } else {
//            Log.d("LOG", "OpenCV Loaded Successfully!");
//        }



           base_dir = android.os.Environment.getStorageDirectory().getAbsolutePath();
           file_name = "gnss_log_" + getCurrentTime();
           Log.d(LOG, file_name);
           file_path = base_dir + File.separator + file_name;

           file = new File(file_path);



    }

    public void logData(String input){
        try {
            // File exist
            if (file.exists() && !file.isDirectory()) {
                fileWriter = new FileWriter(file_path, true);
                csvWriter = new CSVWriter(fileWriter);
            } else {
                file.createNewFile();
                csvWriter = new CSVWriter(new FileWriter(file_path));
                csvWriter.writeNext(new String[]{"Log", "File", "Starting"});

            }

            Log.d(LOG, "logging file now!");
            // write in data
//            String [] data = new Sinput.split(" ").length;
            String [] data = getCurrentTime().split("_");
            csvWriter.writeNext(data);
            csvWriter.close();

        } catch (IOException e){
            e.printStackTrace();
            Log.d(LOG, "unable to log file");
        }
    }


    public String getCurrentTime(){
        String currentDate = new SimpleDateFormat("yy_MM_dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH_mm_ss", Locale.getDefault()).format(new Date());
        return currentDate + "_" + currentTime ;
    }

}
