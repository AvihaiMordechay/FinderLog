package com.havah_avihaim_emanuelm.finderlog;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navbar);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_lost) {
                startActivity(new Intent(this, LostItemsActivity.class));
                return true;
            } else if (itemId == R.id.nav_matches) {
                startActivity(new Intent(this, MatchesActivity.class));
                return true;
            } else if (itemId == R.id.nav_found) {
                startActivity(new Intent(this, FoundItemsActivity.class));
                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(this, ReportsActivity.class));
                return true;
            }
            return false;
        });

    }
}
