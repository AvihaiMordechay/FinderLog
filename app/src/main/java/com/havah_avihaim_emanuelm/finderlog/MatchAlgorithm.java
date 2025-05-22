package com.havah_avihaim_emanuelm.finderlog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.havah_avihaim_emanuelm.finderlog.adapters.Repositories;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.ml_kit.MachineLearningService;

public class MatchAlgorithm {

    private final Context context;
    private final FirestoreService firestoreService;
    private final String mimeType;
    private final Runnable onComplete;

    private String imgTitle;

    public MatchAlgorithm(Context context, FirestoreService firestoreService, String mimeType, Runnable onComplete, String imgTitle) {
        this.context = context;
        this.firestoreService = firestoreService;
        this.mimeType = mimeType;
        this.onComplete = onComplete;
        this.imgTitle = imgTitle;

        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(MachineLearningService.ACTION_ANALYZE_IMAGE);
        ContextCompat.registerReceiver(context, resultReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String imageUri = intent.getStringExtra(MachineLearningService.EXTRA_IMAGE_URI);
            String labels = intent.getStringExtra(MachineLearningService.EXTRA_LABELS);

            if (imageUri != null) {
                FoundItem foundItem = new FoundItem(imgTitle, imageUri, mimeType, labels);
                firestoreService.addItem(foundItem);
                Repositories.getFoundRepo().addItem(foundItem);
                Toast.makeText(context, "Image uploaded and processed successfully", Toast.LENGTH_SHORT).show();

                if (onComplete != null) {
                    onComplete.run();
                }

                // Once done, unregister receiver
                context.unregisterReceiver(this);
            } else {
                Toast.makeText(context, "Image analysis failed", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
