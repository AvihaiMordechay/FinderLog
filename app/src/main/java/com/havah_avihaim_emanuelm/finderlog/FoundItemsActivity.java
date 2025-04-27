package com.havah_avihaim_emanuelm.finderlog;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FoundItemsActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_items);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewFound);
        setupBottomNavigation(bottomNavigationView, R.id.nav_found);

    }
}