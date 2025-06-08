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

// Sets up the Found items list with RecyclerView and bottom navigation;
// manages empty state UI and updates it on data changes.
public class FoundActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found);
        NetworkAwareDataLoader.loadData(this, firestoreService);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewFound);
        setupBottomNavigation(bottomNavigationView, R.id.nav_found);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFound);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemRepository foundRepo = Repositories.getFoundRepo();
        updateEmptyState(recyclerView, foundRepo.getSize()==0);
        ItemAdapter adapter = new ItemAdapter(foundRepo,
                () -> updateEmptyState(recyclerView,foundRepo.getSize()==0),true);
        recyclerView.setAdapter(adapter);

    }

}