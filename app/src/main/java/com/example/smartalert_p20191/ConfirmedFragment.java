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
    private EmergencyGroup adapter;
    private List<Map<String, Object>> groupedEmergencies;
    private static final double MAX_DISTANCE = 200; // Maximum distance in km

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmed, container, false);

        listView = view.findViewById(R.id.emergency_list_confirmed);
        groupedEmergencies = new ArrayList<>();
        adapter = new EmergencyGroup(getContext(), groupedEmergencies);
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

        // Ensure groupedMap is not null before using
        if (groupedMap == null) {
            groupedMap = new HashMap<>();
        }

        // Combine emergencies if they are close to each other
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

            // Ensure mergedGroup is not null before using
            if (mergedGroup == null) {
                mergedGroup = new ArrayList<>();
            }

            groupedEmergencies.addAll(mergedGroup);
        }

        // Ensure groupedEmergencies is not null before using
        if (groupedEmergencies == null) {
            groupedEmergencies = new ArrayList<>();
        }

        this.groupedEmergencies.clear();
        this.groupedEmergencies.addAll(groupedEmergencies);
        adapter.notifyDataSetChanged();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Use Haversine formula or any other distance calculation method
        // This is a placeholder implementation
        return Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2));
    }

    private Map<String, Object> combineEmergencies(List<Map<String, Object>> emergencies) {
        if (emergencies == null || emergencies.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> combinedEmergency = new HashMap<>();
        String type = (String) emergencies.get(0).get("type");
        double latitude = (Double) emergencies.get(0).get("latitude");
        double longitude = (Double) emergencies.get(0).get("longitude");
        int count = emergencies.size();

        combinedEmergency.put("type", type);
        combinedEmergency.put("latitude", latitude);
        combinedEmergency.put("longitude", longitude);
        combinedEmergency.put("count", count);

        return combinedEmergency;
    }
}
