package com.ai2s_lab.gnss_dr.io;

import android.util.Log;
import java.util.Calendar;
import java.util.Date;

public class Logger {

    private String tag = "gnss_log";
    private String base_dir;
    private String file_name;
    private String file_path;

    public Logger(){
           base_dir = android.os.Environment.getStorageDirectory().getAbsolutePath();

           Date current_time = Calendar.getInstance().getTime();
           file_name = "gnss_log_" + current_time.toString();


    }

}
