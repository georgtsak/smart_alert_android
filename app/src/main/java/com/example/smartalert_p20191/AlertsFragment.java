package com.example.smartalert_p20191;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AlertsFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private List<Map<String, Object>> emergencies;
    private CardView infoCardView;
    private TextView dateTextView, commentTextView, locationTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        infoCardView = view.findViewById(R.id.info_card);
        dateTextView = view.findViewById(R.id.dateTextView);
        commentTextView = view.findViewById(R.id.commentTextView);
        locationTextView = view.findViewById(R.id.locationTextView);

        emergencies = new ArrayList<>();
        loadEmergenciesFromFirebase();

        // Απόκρυψη του CardView μέχρι να επιλεχθεί ένας marker
        infoCardView.setVisibility(View.GONE);

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

        LatLng defaultLocation = new LatLng(37.9838, 23.7275); // Αθήνα, Ελλάδα
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 11));

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Ανάκτηση των πληροφοριών από τον marker
                Map<String, Object> emergency = (Map<String, Object>) marker.getTag();
                if (emergency != null) {
                    // Ενημέρωση του CardView με τα δεδομένα
                    String comments = (String) emergency.get("comments");
                    long timestamp = (long) emergency.get("timestamp");
                    double latitude = (double) emergency.get("latitude");
                    double longitude = (double) emergency.get("longitude");

                    String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
                    String locationText = "Lat: " + latitude + ", Lon: " + longitude;

                    dateTextView.setText("Date: " + formattedDate);
                    commentTextView.setText("Comments: " + comments);
                    locationTextView.setText(locationText);

                    infoCardView.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        updateMapMarkers();
    }

    private void updateMapMarkers() {
        googleMap.clear();
        for (Map<String, Object> emergency : emergencies) {
            double latitude = (double) emergency.get("latitude");
            double longitude = (double) emergency.get("longitude");
            String type = (String) emergency.get("type");

            LatLng position = new LatLng(latitude, longitude);
            MarkerOptions markerOptions = new MarkerOptions().position(position).title(type);

            // Επιλογή και δημιουργία εικονιδίου για τον marker από vector asset
            BitmapDescriptor icon;
            switch (type.toLowerCase()) {
                case "fire":
                    icon = bitmapDescriptorFromVector(requireContext(), R.drawable.baseline_local_fire_department_24);
                    break;
                case "earthquake":
                    icon = bitmapDescriptorFromVector(requireContext(), R.drawable.baseline_area_chart_24);
                    break;
                case "flood":
                    icon = bitmapDescriptorFromVector(requireContext(), R.drawable.baseline_flood_24);
                    break;
                default:
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                    break;
            }

            Marker marker = googleMap.addMarker(markerOptions.icon(icon));
            marker.setTag(emergency);  // Αποθήκευση των πληροφοριών του περιστατικού στον marker
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(@NonNull Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
