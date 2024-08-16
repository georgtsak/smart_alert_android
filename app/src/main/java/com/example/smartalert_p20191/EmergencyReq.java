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
    private List<Map<String, Object>> emergencies;
    private LayoutInflater inflater;

    public EmergencyReq(Context context, List<Map<String, Object>> emergencies) {
        this.emergencies = emergencies;
        this.inflater = LayoutInflater.from(context);
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
            convertView = inflater.inflate(R.layout.activity_emergencyreq, parent, false);
        }

        TextView typeTextView = convertView.findViewById(R.id.typeTextView);
        TextView locationTextView = convertView.findViewById(R.id.locationTextView);
        Button detailsButton = convertView.findViewById(R.id.detailsButton);

        Map<String, Object> emergency = emergencies.get(position);
        String type = (String) emergency.get("type");
        double latitude = (double) emergency.get("latitude");
        double longitude = (double) emergency.get("longitude");
        String userId = (String) emergency.get("userId");
        String emergencyId = (String) emergency.get("id");

        typeTextView.setText("Type: " + type);
        locationTextView.setText("Location: " + latitude + ", " + longitude);

        // OnClickListener για το κουμπί
        detailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(inflater.getContext(), Requests2Activity.class);
            intent.putExtra("emergencyId", emergencyId);
            intent.putExtra("type", type);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("userId", userId);
            // Εδώ μπορείτε να προσθέσετε και άλλα δεδομένα αν χρειάζεται
            inflater.getContext().startActivity(intent);
        });

        return convertView;
    }
}
