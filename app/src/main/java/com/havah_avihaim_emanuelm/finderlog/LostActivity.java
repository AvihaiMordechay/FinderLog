package com.havah_avihaim_emanuelm.finderlog;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LostActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewLost);
        setupBottomNavigation(bottomNavigationView, R.id.nav_lost);

    }
}