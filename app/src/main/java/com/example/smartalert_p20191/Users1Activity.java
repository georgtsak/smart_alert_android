package com.example.smartalert_p20191;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Users1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users1);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new SubmissionFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.submission)
            {
                selectedFragment = new SubmissionFragment();
            } else if (itemId == R.id.statistics) {
                selectedFragment = new StatisticsFragment();
            } else if (itemId == R.id.language) {
                selectedFragment = new LanguageFragment();
            }

            return loadFragment(selectedFragment);
        });
    }
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment1, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
