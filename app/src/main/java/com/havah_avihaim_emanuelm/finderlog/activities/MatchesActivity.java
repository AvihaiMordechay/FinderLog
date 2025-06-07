package com.havah_avihaim_emanuelm.finderlog.activities;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.adapters.MatchAdapter;
import com.havah_avihaim_emanuelm.finderlog.repositories.Repositories;
import com.havah_avihaim_emanuelm.finderlog.matches.Match;

import java.util.List;

public class MatchesActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewMatches);
        setupBottomNavigation(bottomNavigationView, R.id.nav_matches);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewMatches);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Match> matches = Repositories.getMatchRepo().getMatches();
        MatchAdapter adapter = new MatchAdapter(this,matches);
        recyclerView.setAdapter(adapter);

    }

}
