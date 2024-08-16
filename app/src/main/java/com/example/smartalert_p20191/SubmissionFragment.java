package com.example.smartalert_p20191;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class SubmissionFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Spinner spinner;
    private EditText commentsEditText, loading1;
    private ImageView photoImageView;
    private Button submitButton;
    private TextView loadingText;


    private Uri filePath;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude;
    private double longitude;
    // permission gia to storage
    private final ActivityResultLauncher<String> requestStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    chooseImage();
                } else {
                    Toast.makeText(getContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
                }
            });
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    filePath = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), filePath);
                        photoImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );
    // permission gia to location
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });
    public SubmissionFragment() {
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_submission, container, false);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        spinner = view.findViewById(R.id.spinner1);
        commentsEditText = view.findViewById(R.id.comments);
        photoImageView = view.findViewById(R.id.photoImageView);
        submitButton = view.findViewById(R.id.button1);
        loadingText = view.findViewById(R.id.loading1);

        // spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // image picker
        photoImageView.setOnClickListener(v -> checkStoragePermission());
        // submit button
        submitButton.setOnClickListener(v -> submitEmergency());
        // location permission
        checkLocationPermission();
        return view;
    }
    // permission gia to location
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    // permission gia to storage
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            chooseImage();
        } else {
            requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    });
        }
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }
    private void submitEmergency() {
        String selectedItem = spinner.getSelectedItem().toString();
        String commentText = commentsEditText.getText().toString().trim();
        String userId = mAuth.getCurrentUser().getUid();
        if (commentText.isEmpty()) {
            Toast.makeText(getContext(), "Please enter details in the comment field", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingText.setText("Loading...");

        if (filePath != null) {
            uploadImageAndSaveEmergency(selectedItem, commentText, userId);
        } else {
            saveEmergency(selectedItem, commentText, userId, null);
        }
    }
    private void uploadImageAndSaveEmergency(String type, String comments, String userId) {
        String imageId = UUID.randomUUID().toString();
        StorageReference ref = storageReference.child("images/" + imageId);
        ref.putFile(filePath)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveEmergency(type, comments, userId, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    loadingText.setText("");
                });
    }

    private void saveEmergency(String type, String comments, String userId, @Nullable String imageUrl) {
        DatabaseReference reference = database.getReference("emergencies");
        String id = reference.push().getKey();
        if (id != null) {
            Map<String, Object> emergency = new HashMap<>();
            emergency.put("type", type);
            emergency.put("comments", comments);
            emergency.put("userId", userId);
            emergency.put("timestamp", System.currentTimeMillis());
            emergency.put("latitude", latitude);
            emergency.put("longitude", longitude);
            if (imageUrl != null) {
                emergency.put("imageUrl", imageUrl);
            }
            reference.child(id).setValue(emergency)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Emergency submitted successfully", Toast.LENGTH_SHORT).show();
                        // Clear fields after submission
                        loadingText.setText("");
                        // katharismos
                        spinner.setSelection(0);
                        commentsEditText.setText("");
                        photoImageView.setImageResource(R.drawable.baseline_add_photo_alternate_24);
                        filePath = null;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to submit emergency", Toast.LENGTH_SHORT).show();
                        loadingText.setText("");
                    });
        }
    }
}