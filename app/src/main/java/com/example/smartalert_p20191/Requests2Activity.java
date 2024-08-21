package com.example.smartalert_p20191;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//exoun thema ta comments --> comments:null
//prepei na emfanizw to image
public class Requests2Activity extends AppCompatActivity {

    private TextView typeText, commentsTextView, locationTextView, statusTextView;
    private Button acceptButton, rejectButton;

    private FirebaseDatabase database;
    private String emergencyId;
    private ImageView emergencyImageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests2);

        typeText = findViewById(R.id.typeText);
        commentsTextView = findViewById(R.id.commentsTextView);
        locationTextView = findViewById(R.id.locationTextView);
        statusTextView = findViewById(R.id.statusTextView);
        emergencyImageView = findViewById(R.id.imageView3);
        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);

        database = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        emergencyId = intent.getStringExtra("emergencyId");
        String type = intent.getStringExtra("type");
        String comments = intent.getStringExtra("comments");
        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);
        int status = intent.getIntExtra("status", 0);
        String imageUrl = intent.getStringExtra("imageUrl");

        typeText.setText("Type: " + type);
        commentsTextView.setText("Comments: " + comments);
        locationTextView.setText("Location: " + latitude + ", " + longitude);
        statusTextView.setText("Status: " + (status == 0 ? "Pending" : status == 1 ? "Accepted" : "Rejected"));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(emergencyImageView);
        } else {
            emergencyImageView.setImageResource(R.drawable.baseline_photo_24);
        }
        acceptButton.setOnClickListener(v -> updateStatus(1));
        rejectButton.setOnClickListener(v -> updateStatus(2));
    }

    private void updateStatus(int status) {
        DatabaseReference reference = database.getReference("emergencies").child(emergencyId);
        reference.child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Requests2Activity.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Requests2Activity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                });
    }
}
