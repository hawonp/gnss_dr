package com.ai2s_lab.gnss_dr.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ai2s_lab.gnss_dr.R;
import com.ai2s_lab.gnss_dr.model.Satellite;

import java.util.ArrayList;
import java.util.List;

public class LogListAdapter extends ArrayAdapter<Satellite> {

    private Context context;

    private ArrayList<Satellite> satellites;

    public LogListAdapter(Context context, ArrayList<Satellite> satellites){
        super(context, R.layout.log_list_item, satellites);

        this.context = context;
        this.satellites = satellites;
    }

    public void updateData(ArrayList<Satellite> satellites){
        this.satellites = satellites;
    }

    public View getView(int position, View view, ViewGroup parent){
        if(view == null){
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.log_list_item, null);

        }
        TextView text_id = view.findViewById(R.id.log_item_id);
        TextView text_type = view.findViewById(R.id.log_item_type);
        TextView text_cno = view.findViewById(R.id.log_item_cno);
        TextView text_elev = view.findViewById(R.id.log_item_elev);
        TextView text_azim = view.findViewById(R.id.log_item_azim);

        Satellite satellite = satellites.get(position);
        text_id.setText(Integer.toString(satellite.getId()));
        text_type.setText(satellite.getType());
        text_cno.setText(Double.toString(satellite.getCno()));
        text_elev.setText(Double.toString(satellite.getElev()));
        text_azim.setText(Double.toString(satellite.getAzim()));

        return view;
    }
}
