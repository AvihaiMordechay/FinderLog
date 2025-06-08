package com.havah_avihaim_emanuelm.finderlog.utils;

import static com.havah_avihaim_emanuelm.finderlog.repositories.Repositories.getLostRepo;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import com.havah_avihaim_emanuelm.finderlog.firebase.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.items.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.items.LostItem;
import com.havah_avihaim_emanuelm.finderlog.repositories.Repositories;

public class NetworkAwareDataLoader {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    public static void loadData(Context context, FirestoreService firestoreService) {
        if (NetworkAwareDataLoader.isNetworkAvailable(context)) {
            if (Repositories.getFoundRepo().needsLoading()) {
                firestoreService.getItems(FoundItem.class, Repositories.getFoundRepo()::setItems);
            }
            if (getLostRepo().needsLoading()) {
                firestoreService.getItems(LostItem.class, getLostRepo()::setItems);
            }
            if (Repositories.getMatchRepo().needsLoading()) {
                firestoreService.getAllMatches(Repositories.getMatchRepo()::setMatches);
            }
        } else {
            new AlertDialog.Builder(context)
                    .setTitle("No Internet Connection")
                    .setMessage("New data could not be loaded.")
                    .setPositiveButton("Retry", (dialog, which) -> loadData(context, firestoreService))
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
