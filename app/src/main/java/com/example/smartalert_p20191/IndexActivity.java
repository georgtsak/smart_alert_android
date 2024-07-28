package com.example.smartalert_p20191;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class IndexActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
    }

    // users --> ta emfanizei ola ( leitourgies: LOG-IN kai SIGN-UP)
    public void users(View view) {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra("showFields", true);
        startActivity(intent);
    }

    //den mporei na ginei kainourgia eggrafh gia zhthmata asfaleias)
    // employees --> mono LOG-IN.
    public void employees(View view) {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra("showFields", false); // hide --> firstname, lastname και create account
        startActivity(intent);
    }
}