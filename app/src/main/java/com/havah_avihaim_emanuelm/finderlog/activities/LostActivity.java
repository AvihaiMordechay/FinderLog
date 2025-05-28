package com.havah_avihaim_emanuelm.finderlog.activities;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.adapters.Item.ItemAdapter;
import com.havah_avihaim_emanuelm.finderlog.adapters.Item.ItemRepository;
import com.havah_avihaim_emanuelm.finderlog.adapters.Repositories;

public class LostActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewLost);
        setupBottomNavigation(bottomNavigationView, R.id.nav_lost);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewLost);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        ItemRepository lostRepo = Repositories.getLostRepo();
        updateEmptyState(recyclerView, lostRepo.getSize()==0);
        ItemAdapter adapter = new ItemAdapter(lostRepo,
                () -> updateEmptyState(recyclerView,lostRepo.getSize()==0),true);
        recyclerView.setAdapter(adapter);

    }
}
