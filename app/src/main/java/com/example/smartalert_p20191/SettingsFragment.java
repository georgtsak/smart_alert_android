package com.example.smartalert_p20191;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
    private Button ButtonBlue;
    private ImageView darkMode1, Language1, Location1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Name1 = view.findViewById(R.id.firstname_lastname);
        Email = view.findViewById(R.id.email);
        locationSwitch = view.findViewById(R.id.locationSwitch);
        ButtonBlue = view.findViewById(R.id.logout_button);
        //darkMode1 = view.findViewById(R.id.darkMode1);
        Language1 = view.findViewById(R.id.Language1);
        Location1 = view.findViewById(R.id.Location1);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadUserData(user.getUid());
        }

        updateLocationSwitch();

        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        });

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
                    String role = document.getString("role");

                    Name1.setText(firstname + " " + lastname);
                    Email.setText(email);
                    updateBg(role);

                } else {
                }
            } else {
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

    private void updateBg(String role) {
        if ("registered_user".equals(role)) {
            ButtonBlue.setBackgroundResource(R.drawable.color_red_button);
            //darkMode1.setBackgroundResource(R.drawable.bg_red_radius);
            Language1.setBackgroundResource(R.drawable.bg_red_radius);
            Location1.setBackgroundResource(R.drawable.bg_red_radius);
        } else if ("employee".equals(role)) {
            ButtonBlue.setBackgroundResource(R.drawable.color_blue_button);
            //darkMode1.setBackgroundResource(R.drawable.bg_blue_radius);
            Language1.setBackgroundResource(R.drawable.bg_blue_radius);
            Location1.setBackgroundResource(R.drawable.bg_blue_radius);

        }
    }
}
