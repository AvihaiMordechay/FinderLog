package com.havah_avihaim_emanuelm.finderlog.activities;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.adapters.ItemAdapter;
import com.havah_avihaim_emanuelm.finderlog.adapters.Repositories;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.Item;
import java.util.List;

public class FoundActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewFound);
        setupBottomNavigation(bottomNavigationView, R.id.nav_found);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFound);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Item> foundItems = Repositories.getFoundRepo().getCachedItems();
        recyclerView.setAdapter(new ItemAdapter(foundItems));

    }


}