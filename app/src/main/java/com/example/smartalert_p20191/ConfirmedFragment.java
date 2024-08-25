package com.example.smartalert_p20191;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmedFragment extends Fragment {

    private ListView listView;
    private EmergencyReq adapter;
    private List<Map<String, Object>> emergencies;
    private static final double MAX_DISTANCE = 200; // Maximum distance in km

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmed, container, false);

        listView = view.findViewById(R.id.emergency_list_confirmed);
        emergencies = new ArrayList<>();
        adapter = new EmergencyReq(getContext(), emergencies);
        listView.setAdapter(adapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("emergencies");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Map<String, Object>> rawEmergencies = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Map<String, Object> emergency = (Map<String, Object>) dataSnapshot.getValue();
                    if (emergency != null && (Long) emergency.get("status") == 1) { // Only accepted emergencies
                        rawEmergencies.add(emergency);
                    }
                }
                groupEmergencies(rawEmergencies);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ConfirmedFragment", "Database error: " + error.getMessage());
                Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void groupEmergencies(List<Map<String, Object>> rawEmergencies) {
        if (rawEmergencies == null) {
            rawEmergencies = new ArrayList<>();
        }

        List<Map<String, Object>> groupedEmergencies = new ArrayList<>();
        Map<String, List<Map<String, Object>>> groupedMap = new HashMap<>();

        for (Map<String, Object> emergency : rawEmergencies) {
            if (emergency == null) {
                continue;
            }

            String type = (String) emergency.get("type");
            double latitude = (Double) emergency.get("latitude");
            double longitude = (Double) emergency.get("longitude");

            String key = type;
            groupedMap.putIfAbsent(key, new ArrayList<>());
            List<Map<String, Object>> group = groupedMap.get(key);
            if (group != null) {
                group.add(emergency);
            }
        }

        if (groupedMap == null) {
            groupedMap = new HashMap<>();
        }
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedMap.entrySet()) {
            List<Map<String, Object>> group = entry.getValue();
            List<Map<String, Object>> mergedGroup = new ArrayList<>();

            while (!group.isEmpty()) {
                Map<String, Object> baseEmergency = group.remove(0);
                List<Map<String, Object>> closeEmergencies = new ArrayList<>();
                closeEmergencies.add(baseEmergency);

                for (Map<String, Object> otherEmergency : new ArrayList<>(group)) {
                    double distance = calculateDistance(
                            (Double) baseEmergency.get("latitude"), (Double) baseEmergency.get("longitude"),
                            (Double) otherEmergency.get("latitude"), (Double) otherEmergency.get("longitude")
                    );
                    if (distance <= MAX_DISTANCE) {
                        closeEmergencies.add(otherEmergency);
                        group.remove(otherEmergency);
                    }
                }

                Map<String, Object> mergedEmergency = combineEmergencies(closeEmergencies);
                mergedGroup.add(mergedEmergency);
            }

            // mergedGroup --> not null
            if (mergedGroup == null) {
                mergedGroup = new ArrayList<>();
            }

            groupedEmergencies.addAll(mergedGroup);
        }

        // groupedEmergencies --> not null
        if (groupedEmergencies == null) {
            groupedEmergencies = new ArrayList<>();
        }

        emergencies.clear();
        emergencies.addAll(groupedEmergencies);
        adapter.notifyDataSetChanged();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
    }

    private Map<String, Object> combineEmergencies(List<Map<String, Object>> emergencies) {
        if (emergencies == null || emergencies.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> combinedEmergency = new HashMap<>();
        combinedEmergency.put("type", emergencies.get(0).get("type"));
        combinedEmergency.put("latitude", emergencies.get(0).get("latitude"));
        combinedEmergency.put("longitude", emergencies.get(0).get("longitude"));
        combinedEmergency.put("count", emergencies.size());
        return combinedEmergency;
    }
}
