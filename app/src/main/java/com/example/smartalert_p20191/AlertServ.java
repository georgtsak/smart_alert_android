package com.example.smartalert_p20191;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlertServ extends Service {

    private Location userLocation;
    private DatabaseReference alertsRef;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        alertsRef = FirebaseDatabase.getInstance().getReference("alerts");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLocation = location;
                        } else {
                            Toast.makeText(AlertServ.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        ChildEventListener alertListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Alert alert = dataSnapshot.getValue(Alert.class);
                if (alert != null && isWithinProximity(alert)) {
                    showEmergencyAlert();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        alertsRef.addChildEventListener(alertListener);
    }

    private boolean isWithinProximity(Alert alert) {
        if (userLocation == null) return false;

        Location alertLocation = new Location("");
        alertLocation.setLatitude(alert.getLatitude());
        alertLocation.setLongitude(alert.getLongitude());

        float distanceInMeters = userLocation.distanceTo(alertLocation);
        return distanceInMeters <= 20000; // 20 xiliometra
    }

    private void showEmergencyAlert() {
        Toast.makeText(this, "Emergency alert nearby!", Toast.LENGTH_LONG).show();
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(this, notification);
        ringtone.play();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Service is started
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up resources, if needed
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

