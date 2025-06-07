package com.havah_avihaim_emanuelm.finderlog.activities;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.firebase.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.StorageService;

/**
 * BaseActivity is an abstract class that provides shared functionality for all activities in the app,
 * including common services and UI setup such as bottom navigation and empty state handling.
 */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * Singleton instance of FirestoreService for accessing Firebase Firestore.
     */
    protected static final FirestoreService firestoreService = FirestoreService.getSharedInstance();
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

    protected void updateEmptyState(RecyclerView recyclerView, boolean isEmpty) {
        Log.d("updateEmptyState", "In updateEmptyState, isEmpty = " + isEmpty);

        TextView emptyMessage = findViewById(R.id.emptyMessage);
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setContentDescription(getString(R.string.empty_list));
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
            recyclerView.setContentDescription(null);
        }
    }

}
