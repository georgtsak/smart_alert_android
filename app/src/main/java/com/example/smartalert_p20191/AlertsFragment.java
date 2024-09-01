package com.example.smartalert_p20191;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertsFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private List<Map<String, Object>> emergencies;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        emergencies = new ArrayList<>();
        loadEmergenciesFromFirebase();

        return view;
    }

    private void loadEmergenciesFromFirebase() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("emergencies");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                emergencies.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> emergency = (Map<String, Object>) snapshot.getValue();
                    if (emergency != null) {
                        emergencies.add(emergency);
                    }
                }
                // Ενημέρωση του χάρτη μόλις φορτωθούν τα δεδομένα
                if (googleMap != null) {
                    updateMapMarkers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AlertsFragment", "Failed to load emergencies: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(requireContext());
        this.googleMap = googleMap;

        // Μπορείς να μετακινήσεις την κάμερα σε μια προεπιλεγμένη τοποθεσία αν θέλεις
        LatLng defaultLocation = new LatLng(37.9838, 23.7275); // Συντεταγμένες για Αθήνα, Ελλάδα
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        // Προσθήκη markers μόλις ο χάρτης είναι έτοιμος
        updateMapMarkers();
    }

    private void updateMapMarkers() {
        googleMap.clear();
        for (Map<String, Object> emergency : emergencies) {
            double latitude = (double) emergency.get("latitude");
            double longitude = (double) emergency.get("longitude");
            String type = (String) emergency.get("type");

            LatLng position = new LatLng(latitude, longitude);
            googleMap.addMarker(new MarkerOptions().position(position).title(type));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
