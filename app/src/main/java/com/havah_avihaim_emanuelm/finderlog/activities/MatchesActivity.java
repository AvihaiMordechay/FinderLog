package com.havah_avihaim_emanuelm.finderlog.activities;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.havah_avihaim_emanuelm.finderlog.R;

public class MatchesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewMatches);
        setupBottomNavigation(bottomNavigationView, R.id.nav_matches);
    }
}