package com.example.smartalert_p20191;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class EmergencyGroup extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> groupedEmergencies;

    public EmergencyGroup(Context context, List<Map<String, Object>> groupedEmergencies) {
        this.context = context;
        this.groupedEmergencies = groupedEmergencies;
    }

    @Override
    public int getCount() {
        return groupedEmergencies.size();
    }

    @Override
    public Object getItem(int position) {
        return groupedEmergencies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_emergencygroup, parent, false);
        }

        TextView typeTextView = convertView.findViewById(R.id.groupedTypeTextView);
        TextView locationTextView = convertView.findViewById(R.id.groupedLocationTextView);
        TextView countTextView = convertView.findViewById(R.id.groupedCountTextView);

        Map<String, Object> emergency = groupedEmergencies.get(position);

        String type = (String) emergency.get("type");
        double latitude = (Double) emergency.get("latitude");
        double longitude = (Double) emergency.get("longitude");
        int count = (Integer) emergency.get("count");

        typeTextView.setText("Type: " + type);
        locationTextView.setText("Location: " + latitude + ", " + longitude);
        countTextView.setText("Count: " + count);

        return convertView;
    }
}
