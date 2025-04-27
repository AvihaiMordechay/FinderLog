package com.havah_avihaim_emanuelm.finderlog;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FoundActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewFound);
        setupBottomNavigation(bottomNavigationView, R.id.nav_found);

    }
}