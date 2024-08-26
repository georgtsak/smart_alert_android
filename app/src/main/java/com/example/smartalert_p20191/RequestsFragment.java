package com.example.smartalert_p20191;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
    private DatabaseReference reference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        listView = view.findViewById(R.id.emergency_list);
        emergencies = new ArrayList<>();
        adapter = new EmergencyReq(requireContext(), emergencies);
        listView.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference("emergencies");

        loadEmergenciesFromFirebase();

        return view;
    }

    private void loadEmergenciesFromFirebase() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                emergencies.clear();
                List<Map<String, Object>> tempList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> emergency = (Map<String, Object>) snapshot.getValue();
                    if (emergency != null) {
                        String type = (String) emergency.get("type");
                        double latitude = (double) emergency.get("latitude");
                        double longitude = (double) emergency.get("longitude");
                        String userId = (String) emergency.get("userId");

                        emergency.put("id", snapshot.getKey());
                        String displayText = type + " - Lat: " + latitude + ", Lon: " + longitude + " (User ID: " + userId + ")";
                        emergency.put("displayText", displayText);
                        tempList.add(emergency);
                    }
                }

                // Reverse the list to show the most recent emergencies first
                Collections.reverse(tempList);
                emergencies.addAll(tempList);
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("RequestsFragment", "Failed to load emergencies: " + databaseError.getMessage());
            }
        });
    }
}
