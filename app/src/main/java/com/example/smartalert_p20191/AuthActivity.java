package com.example.smartalert_p20191;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    private TextView switchText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isSignUpMode = false;
    private String expectedRole;

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
        switchText = findViewById(R.id.switch_text);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        expectedRole = getIntent().getStringExtra("role");

        updateUIForAuthMode();

        createAccount.setOnClickListener(v -> createAccount());

        loginButton.setOnClickListener(v -> login(email.getText().toString().trim(), password.getText().toString().trim()));

        switchText.setOnClickListener(v -> {
            isSignUpMode = !isSignUpMode;
            updateUIForAuthMode();
        });
    }

    private void createAccount() {
        String firstNameInput = firstname.getText().toString().trim();
        String lastNameInput = lastname.getText().toString().trim();
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        if (firstNameInput.isEmpty() || lastNameInput.isEmpty()) {
            Toast.makeText(this, "Please enter first name and last name", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUser(user, firstNameInput, lastNameInput, emailInput);
                            Toast.makeText(AuthActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                            login(emailInput, passwordInput); // Απευθείας login μετά την επιτυχημένη εγγραφή
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e("AuthActivity", "Authentication failed: " + errorMessage);
                        Toast.makeText(AuthActivity.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUser(FirebaseUser firebaseUser, String firstname, String lastname, String email) {
        String userId = firebaseUser.getUid();
        String role = "registered_user"; // default role gia olous tous neous xrhstes

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("firstname", firstname);
        user.put("lastname", lastname);
        user.put("role", role);

        DocumentReference documentReference = db.collection("users").document(userId);
        documentReference.set(user)
                .addOnSuccessListener(aVoid -> Log.e("AuthActivity", "User data successfully saved."))
                .addOnFailureListener(e -> Log.e("AuthActivity", "Failed to save user data.", e));
    }


    // prepei na valoyme 2 diaforetika login1 (koubi1) kai login2(koubi2)
    // etsi wste na elegxoume role
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
                        if ("registered_user".equals(role)) {
                            startActivity(new Intent(AuthActivity.this, UserActivity.class));
                        } else if ("employee".equals(role)) {
                            startActivity(new Intent(AuthActivity.this, EmployeeActivity.class));
                        }
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AuthActivity.this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show());
    }

    private void updateUIForAuthMode() {
        if (isSignUpMode) {
            firstname.setVisibility(View.VISIBLE);
            lastname.setVisibility(View.VISIBLE);
            createAccount.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            switchText.setText("Έχετε ήδη λογαριασμό; Συνδεθείτε");
        } else {
            firstname.setVisibility(View.GONE);
            lastname.setVisibility(View.GONE);
            createAccount.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            switchText.setText("Δεν έχετε λογαριασμό; Δημιουργήστε έναν");
        }
    }
}
