package com.example.smartalert_p20191;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestsFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> emergencies;
    private DatabaseReference reference;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        listView = view.findViewById(R.id.emergency_list);
        emergencies = new ArrayList<>();
        adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, emergencies) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                //text.setTextColor(Color.WHITE);
                return view;
            }
        };
        listView.setAdapter(adapter);

        // Σύνδεση με Firebase Realtime Database για περιστατικά
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference("emergencies");

        // Σύνδεση με Firestore για ανάκτηση στοιχείων χρηστών
        firestore = FirebaseFirestore.getInstance();

        loadEmergenciesFromFirebase();

        return view;
    }

    private void loadEmergenciesFromFirebase() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                emergencies.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> emergency = (Map<String, Object>) snapshot.getValue();
                    if (emergency != null) {
                        String type = (String) emergency.get("type");
                        double latitude = (double) emergency.get("latitude");
                        double longitude = (double) emergency.get("longitude");
                        String userId = (String) emergency.get("userId");

                        // Ανάκτηση του firstname και lastname από το Firestore
                        fetchUserNameAndDisplay(type, latitude, longitude, userId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("RequestsFragment", "Failed to load emergencies: " + databaseError.getMessage());
            }
        });
    }

    private void fetchUserNameAndDisplay(String type, double latitude, double longitude, String userId) {
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String firstname = documentSnapshot.getString("firstname");
                String lastname = documentSnapshot.getString("lastname");

                if (firstname != null && lastname != null) {

                    String item = type + " - Lat: " + latitude + ", Lon: " + longitude + " (User: " + firstname + " " + lastname + ")";
                    emergencies.add(item);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("RequestsFragment", "Firstname or Lastname is null for userId: " + userId);
                }
            } else {
                Log.e("RequestsFragment", "Document does not exist for userId: " + userId);
            }
        }).addOnFailureListener(e -> {
            // Χειρισμός σφαλμάτων
            Log.e("RequestsFragment", "Failed to fetch user data for userId: " + userId, e);
            String item = type + " - Lat: " + latitude + ", Lon: " + longitude + " (User: " + userId + ")";
            emergencies.add(item);
            adapter.notifyDataSetChanged();
        });
    }
}
