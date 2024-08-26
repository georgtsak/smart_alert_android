package com.example.smartalert_p20191;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
        ImageView imageView = convertView.findViewById(R.id.groupedImageView);
        Button alertButton = convertView.findViewById(R.id.alertButton);


        Map<String, Object> emergency = groupedEmergencies.get(position);

        String type = (String) emergency.get("type");
        double latitude = (Double) emergency.get("latitude");
        double longitude = (Double) emergency.get("longitude");
        int count = (Integer) emergency.get("count");

        typeTextView.setText("Type: " + type);
        locationTextView.setText("Location: " + latitude + ", " + longitude);
        countTextView.setText("Count: " + count);

        if (count > 9) {
            imageView.setImageResource(R.drawable.baseline_filter_4_24); // level 4 - red
            alertButton.setVisibility(View.VISIBLE);
        } else if (count > 6) {
            imageView.setImageResource(R.drawable.baseline_filter_3_24); // level 3 - orange
            alertButton.setVisibility(View.GONE); // hide button
        } else if (count > 3) {
            imageView.setImageResource(R.drawable.baseline_filter_2_24); // level 2 - yellow
            alertButton.setVisibility(View.GONE); // hide
        } else {
            imageView.setImageResource(R.drawable.baseline_filter_1_24); // level 1 - green
            alertButton.setVisibility(View.GONE); // hide
        }

        return convertView;
    }
}
