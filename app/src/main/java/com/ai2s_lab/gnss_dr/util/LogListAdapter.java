package com.ai2s_lab.gnss_dr.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ai2s_lab.gnss_dr.R;

import java.util.List;

public class LogListAdapter extends ArrayAdapter<String> {

    private Activity activity;

    private String id;
    private String type;
    private String cno;
    private String elev;
    private String azim;

    public LogListAdapter(Activity activity, String id, String type, String cno, String elev, String azim){
        super(activity, R.layout.log_list_item);

        this.activity = activity;
        this.id = id;
        this.type = type;
        this.cno = cno;
        this.elev = elev;
        this.azim = azim;
    }

    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater= activity.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.log_list_item, null,true);

        TextView text_id = rowView.findViewById(R.id.log_item_id);
        TextView text_type = rowView.findViewById(R.id.log_item_type);
        TextView text_cno = rowView.findViewById(R.id.log_item_cno);
        TextView text_elev = rowView.findViewById(R.id.log_item_elev);
        TextView text_azim = rowView.findViewById(R.id.log_item_azim);

        text_id.setText(id);
        text_type.setText(type);
        text_cno.setText(cno);
        text_elev.setText(elev);
        text_azim.setText(azim);

        return rowView;
    }
}
