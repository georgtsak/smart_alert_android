package com.example.smartalert_p20191;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Users1Activity extends AppCompatActivity
        implements BottomNavigationView
        .OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"Plummura", "Purkagia", "Seismos"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        bottomNavigationView
                = findViewById(R.id.bottomNavigationView);

        bottomNavigationView
                .setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.submission);
    }
    SubmissionFragment submissionFragment = new SubmissionFragment();
    StatisticsFragment statisticsFragment = new StatisticsFragment();
    LanguageFragment languageFragment = new LanguageFragment();
    @Override
    public boolean
    onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.submission:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.Fragment1, submissionFragment)
                        .commit();
                return true;

            case R.id.statistics:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.Fragment1, statisticsFragment)
                        .commit();
                return true;

            case R.id.language:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.Fragment1, languageFragment)
                        .commit();
                return true;
        }
        return false;
    }
}
