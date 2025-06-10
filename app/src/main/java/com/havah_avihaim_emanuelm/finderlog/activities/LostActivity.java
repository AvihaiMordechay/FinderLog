package com.havah_avihaim_emanuelm.finderlog.activities;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.adapters.ItemAdapter;
import com.havah_avihaim_emanuelm.finderlog.repositories.ItemRepository;
import com.havah_avihaim_emanuelm.finderlog.repositories.Repositories;
import com.havah_avihaim_emanuelm.finderlog.utils.NetworkAwareDataLoader;

// Initializes Lost items list with RecyclerView and bottom navigation;
// updates UI if the lost items list is empty and refreshes on data changes.
public class LostActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewLost);
        setupBottomNavigation(bottomNavigationView, R.id.nav_lost);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewLost);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        NetworkAwareDataLoader.loadData(this, firestoreService, () -> runOnUiThread(() -> {
            ItemRepository lostRepo = Repositories.getLostRepo();
            updateEmptyState(recyclerView, lostRepo.getSize()==0);
            ItemAdapter adapter = new ItemAdapter(lostRepo,
                    () -> updateEmptyState(recyclerView,lostRepo.getSize()==0),true);
            recyclerView.setAdapter(adapter);
        }));
    }

}
