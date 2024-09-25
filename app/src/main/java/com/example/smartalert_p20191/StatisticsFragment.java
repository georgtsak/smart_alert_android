package com.example.smartalert_p20191;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private TextView acceptedCount, rejectedCount, pendingCount;
    private ListView statsListView;
    private EmergencyReq adapter;
    private List<Map<String, Object>> emergencies;
    private DatabaseReference reference;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        acceptedCount = view.findViewById(R.id.acceptedTextView);
        rejectedCount = view.findViewById(R.id.rejectedTextView);
        pendingCount = view.findViewById(R.id.pendingTextView);
        statsListView = view.findViewById(R.id.stats_list);

        emergencies = new ArrayList<>();
        adapter = new EmergencyReq(requireContext(), emergencies);
        statsListView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        reference = FirebaseDatabase.getInstance().getReference("emergencies");
        loadEmergencyData();

        return view;
    }

    private void loadEmergencyData() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int accepted = 0;
                int rejected = 0;
                int pending = 0;

                emergencies.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> emergency = (Map<String, Object>) snapshot.getValue();

                    if (emergency != null) {
                        String userId = (String) emergency.get("userId");
                        if (userId != null && userId.equals(currentUserId)) {
                            emergencies.add(emergency);

                            if (emergency.get("status") != null) {
                                long status = (long) emergency.get("status");
                                if (status == 1) {
                                    accepted++;
                                } else if (status == 2) {
                                    rejected++;
                                } else if (status == 0) {
                                    pending++;
                                }
                            }
                        }
                    }
                }

                acceptedCount.setText(String.valueOf(accepted));
                rejectedCount.setText(String.valueOf(rejected));
                pendingCount.setText(String.valueOf(pending));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("StatisticsFragment", "Failed to load emergencies: " + databaseError.getMessage());
            }
        });
    }
}
