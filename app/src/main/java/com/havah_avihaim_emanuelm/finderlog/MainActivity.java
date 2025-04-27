package com.havah_avihaim_emanuelm.finderlog;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class MainActivity extends AppCompatActivity {
    protected void setupBottomNavigation(BottomNavigationView bottomNavigationView, int currentItemId) {
        bottomNavigationView.setSelectedItemId(currentItemId);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentItemId) {
                return true;
            }

            Class<?> targetActivity = null;
            if (itemId == R.id.nav_matches) {
                targetActivity = MatchesActivity.class;
            } else if (itemId == R.id.nav_lost) {
                targetActivity = LostItemsActivity.class;
            }else if(itemId == R.id.nav_found){
                targetActivity = FoundItemsActivity.class;
            }else if(itemId == R.id.nav_reports){
                targetActivity = ReportsActivity.class;
            }

            if (targetActivity != null) {
                startActivity(new Intent(this, targetActivity));
                overridePendingTransition(0, 0);
                finish();
            }
            return true;
        });
    }

}
