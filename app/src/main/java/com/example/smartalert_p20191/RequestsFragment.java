package com.example.smartalert_p20191;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RequestsFragment extends Fragment {

    private ListView listView;
    private EmergencyReq adapter;
    private List<Map<String, Object>> emergencies;
    private List<Map<String, Object>> all;
    private DatabaseReference reference;
    private Spinner spinner2;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        listView = view.findViewById(R.id.emergency_list);
        emergencies = new ArrayList<>();
        all = new ArrayList<>();
        adapter = new EmergencyReq(requireContext(), emergencies);
        listView.setAdapter(adapter);

        spinner2 = view.findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.spinner_items2, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(spinnerAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference("emergencies");

        loadEmergenciesFromFirebase();

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterEmergenciesByStatus(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
        return view;
    }

    private void loadEmergenciesFromFirebase() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                emergencies.clear();
                all.clear(); // Clear the list to avoid duplication

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> emergency = (Map<String, Object>) snapshot.getValue();
                    if (emergency != null) {
                        Long status = (Long) emergency.get("status");
                        if (status != null) {
                            String type = (String) emergency.get("type");
                            double latitude = (double) emergency.get("latitude");
                            double longitude = (double) emergency.get("longitude");
                            String userId = (String) emergency.get("userId");

                            emergency.put("id", snapshot.getKey());
                            String displayText = type + " - Lat: " + latitude + ", Lon: " + longitude + " (User ID: " + userId + ")";
                            emergency.put("displayText", displayText);

                            emergencies.add(emergency);
                            all.add(emergency);
                        }
                    }
                }

                Collections.reverse(emergencies);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("RequestsFragment", "Failed to load emergencies: " + databaseError.getMessage());
            }
        });
    }

    private void filterEmergenciesByStatus(int position) {
        emergencies.clear();

        if (position == 0) { //all
            emergencies.addAll(all);
        } else if (position == 1) { // accepted - status:1
            for (Map<String, Object> emergency : all) {
                if (emergency.get("status") != null && (long) emergency.get("status") == 1) {
                    emergencies.add(emergency);
                }
            }
        } else if (position == 2) { // rejected - status:2
            for (Map<String, Object> emergency : all) {
                if (emergency.get("status") != null && (long) emergency.get("status") == 2) {
                    emergencies.add(emergency);
                }
            }
        } else if (position == 3) { // pending - status:0
            for (Map<String, Object> emergency : all) {
                if (emergency.get("status") != null && (long) emergency.get("status") == 0) {
                    emergencies.add(emergency);
                }
            }
        }

        adapter.notifyDataSetChanged(); // refresh
    }

}
