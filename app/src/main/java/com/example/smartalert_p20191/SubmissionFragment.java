package com.example.smartalert_p20191;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Spinner spinner;
    private EditText comments;
    private ImageView photoImageView;
    private Button submitButton;

    private Uri filePath;

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

    public SubmissionFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_submission, container, false);

        // Initialize Firebase Auth, Realtime Database, and Storage
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        spinner = view.findViewById(R.id.spinner1);
        comments = view.findViewById(R.id.comments);
        photoImageView = view.findViewById(R.id.photoImageView);
        submitButton = view.findViewById(R.id.button1);

        // Set up spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set up photo selection
        photoImageView.setOnClickListener(v -> chooseImage());

        // Set up button click listener
        submitButton.setOnClickListener(v -> submitEmergency());

        return view;
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void submitEmergency() {
        String selectedItem = spinner.getSelectedItem().toString();
        String commentText = comments.getText().toString().trim();
        String userId = mAuth.getCurrentUser().getUid();

        if (commentText.isEmpty()) {
            Toast.makeText(getContext(), "Please enter details in the comment field", Toast.LENGTH_SHORT).show();
            return;
        }

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
                    Toast.makeText(getContext(), "Emergency submitted successfully", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveEmergency(String type, String comments, String userId, @Nullable String imageUrl) {
        DatabaseReference reference = database.getReference("emergencies");

        String id = reference.push().getKey(); // Generate a unique ID for the new entry

        if (id != null) {
            Map<String, Object> emergency = new HashMap<>();
            emergency.put("type", type);
            emergency.put("comments", comments);
            emergency.put("userId", userId);
            emergency.put("timestamp", System.currentTimeMillis());
            if (imageUrl != null) {
                emergency.put("imageUrl", imageUrl);
            }

            reference.child(id).setValue(emergency)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Emergency submitted successfully", Toast.LENGTH_SHORT).show();
                        // Clear fields after submission
                        spinner.setSelection(0);
                        //comments.setText("");
                        photoImageView.setImageResource(R.drawable.baseline_add_photo_alternate_24);
                        filePath = null;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to submit emergency", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
