package com.example.smartalert_p20191;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    private TextView Name1, Email;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Switch locationSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Name1 = view.findViewById(R.id.firstname_lastname);
        Email = view.findViewById(R.id.email);
        locationSwitch = view.findViewById(R.id.locationSwitch);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadUserData(user.getUid());
        }

        updateLocationSwitch();

        return view;
    }

    private void loadUserData(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String firstname = document.getString("firstname");
                    String lastname = document.getString("lastname");
                    String email = document.getString("email");

                    Name1.setText(firstname + " " + lastname);
                    Email.setText(email);
                } else {
                    // handle case where document does not exist
                }
            } else {
                // handle task failure
            }
        });
    }

    private void updateLocationSwitch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationSwitch.setChecked(true);
        } else {
            locationSwitch.setChecked(false);
        }

    }
}
