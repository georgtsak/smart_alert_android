package com.example.smartalert_p20191;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class EmergencyReq extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> emergencies;

    public EmergencyReq(Context context, List<Map<String, Object>> emergencies) {
        this.context = context;
        this.emergencies = emergencies;
    }

    @Override
    public int getCount() {
        return emergencies.size();
    }

    @Override
    public Object getItem(int position) {
        return emergencies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_emergencyreq, parent, false);
        }

        TextView typeTextView = convertView.findViewById(R.id.typeTextView);
        TextView locationTextView = convertView.findViewById(R.id.locationTextView);
        TextView userIdTextView = convertView.findViewById(R.id.userIdTextView);
        Button detailsButton = convertView.findViewById(R.id.detailsButton);

        Map<String, Object> emergency = emergencies.get(position);

        String type = (String) emergency.get("type");
        double latitude = (Double) emergency.get("latitude");
        double longitude = (Double) emergency.get("longitude");
        String userId = (String) emergency.get("userId");

        typeTextView.setText("Type: " + type);
        locationTextView.setText("Location: " + latitude + ", " + longitude);
        userIdTextView.setText("User ID: " + userId);

        detailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, Requests2Activity.class);

            intent.putExtra("emergencyId", (String) emergency.get("id"));
            intent.putExtra("type", type);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("status", (Integer) emergency.get("status"));
            intent.putExtra("comments", (String) emergency.get("comments"));
            intent.putExtra("imageUrl", (String) emergency.get("imageUrl"));

            context.startActivity(intent);
        });

        return convertView;
    }
}
