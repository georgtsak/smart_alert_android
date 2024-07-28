package com.example.smartalert_p20191;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

//login kai signup leitourgies
public class AuthActivity extends AppCompatActivity {
    private EditText firstname, lastname;
    private Button createAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        firstname = findViewById(R.id.firstname);
        lastname = findViewById(R.id.lastname);
        createAccount = findViewById(R.id.button2);

        boolean showFields = getIntent().getBooleanExtra("showFields", true);

        if (!showFields) {
            firstname.setVisibility(View.GONE);
            lastname.setVisibility(View.GONE);
            createAccount.setVisibility(View.GONE);
        }
    }
}