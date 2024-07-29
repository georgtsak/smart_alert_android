package com.example.smartalert_p20191;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {
    private EditText firstname, lastname, email, password;
    private Button createAccount, loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        firstname = findViewById(R.id.firstname);
        lastname = findViewById(R.id.lastname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        createAccount = findViewById(R.id.button2);
        loginButton = findViewById(R.id.button1);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        boolean showFields = getIntent().getBooleanExtra("showFields", true);
        if (!showFields) {
            firstname.setVisibility(View.GONE);
            lastname.setVisibility(View.GONE);
            createAccount.setVisibility(View.GONE);
        }

        // Λειτουργία για την εγγραφή νέου χρήστη
        createAccount.setOnClickListener(v -> create_account(email.getText().toString(), password.getText().toString()));

        // Λειτουργία για τη σύνδεση υπάρχοντος χρήστη
        loginButton.setOnClickListener(v -> login(email.getText().toString(), password.getText().toString()));
    }

    private void create_account(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToFirestore(user);
                    } else {
                        Toast.makeText(AuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        String role = "user";

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);

        DocumentReference documentReference = db.collection("users").document(userId);
        documentReference.set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AuthActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    updateUI(firebaseUser);
                })
                .addOnFailureListener(e -> Toast.makeText(AuthActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show());
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(AuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            checkUserRole(user);
        }
    }

    private void checkUserRole(FirebaseUser user) {
        String userId = user.getUid();
        DocumentReference documentReference = db.collection("users").document(userId);
        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("user".equals(role)) {
                            startActivity(new Intent(AuthActivity.this, UserActivity.class));
                        } else if ("employee".equals(role)) {
                            startActivity(new Intent(AuthActivity.this, EmployeeActivity.class));
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AuthActivity.this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show());
    }
}
