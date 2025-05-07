package com.havah_avihaim_emanuelm.finderlog.activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.storage.StorageService;

public abstract class BaseActivity extends AppCompatActivity {
    protected static final FirestoreService firestoreService = new FirestoreService();
    protected static final StorageService storageService = new StorageService();
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
                targetActivity = LostActivity.class;
            }else if(itemId == R.id.nav_found){
                targetActivity = FoundActivity.class;
            }else if(itemId == R.id.nav_reports){
                targetActivity = MainActivity.class;
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
