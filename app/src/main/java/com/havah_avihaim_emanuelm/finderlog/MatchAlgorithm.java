package com.havah_avihaim_emanuelm.finderlog;

import static com.havah_avihaim_emanuelm.finderlog.adapters.Repositories.getFoundRepo;
import static com.havah_avihaim_emanuelm.finderlog.adapters.Repositories.getLostRepo;
import static com.havah_avihaim_emanuelm.finderlog.adapters.Repositories.getMatchRepo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.Item;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.LostItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.Match;
import com.havah_avihaim_emanuelm.finderlog.firebase.ml_kit.MachineLearningService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MatchAlgorithm {

    private Context context;
    private final FirestoreService firestoreService;
    private String mimeType;
    private Runnable onComplete;
    private String imgTitle;

    public MatchAlgorithm(Context context, FirestoreService firestoreService, String mimeType, Runnable onComplete, String imgTitle) {
        this.context = context;
        this.firestoreService = firestoreService;
        this.mimeType = mimeType;
        this.onComplete = onComplete;
        this.imgTitle = imgTitle;

        registerReceiver();
    }

    public MatchAlgorithm(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
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
                getFoundRepo().addItem(foundItem);
                Toast.makeText(context, "Image uploaded and processed successfully", Toast.LENGTH_SHORT).show();

                if (onComplete != null) {
                    onComplete.run();
                }

                if (labels == null) return;
                new Thread(() -> startMatchingThread(convertToList(labels), imageUri)).start();

                // Once done, unregister receiver
                context.unregisterReceiver(this);
            } else {
                Toast.makeText(context, "Image analysis failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void startMatchingThread(List<String> labels, String imageUri) {
        List<Item> lostItems = getLostRepo().getCachedItems();
        List<LostItem> matchLostItems = new ArrayList<>();
        Match match = new Match(imageUri, this.imgTitle);

        for (Item item : lostItems) {
            List<String> itemDescription = convertToList(item.getDescription());
            for (String word : itemDescription) {
                if (labels.contains(word)) {
                    matchLostItems.add((LostItem) item);
                    break;
                }
            }
        }
        match.setLostItems(matchLostItems);
        firestoreService.addMatch(match);
    }

    public void startMatchingThread(List<String> labels, LostItem lostItem) {
        List<Item> foundItems = getFoundRepo().getCachedItems();
        List<Match> matches = getMatchRepo().getMatches();


        for (Item foundItem : foundItems) {
            List<String> foundDescription = convertToList(foundItem.getDescription());

            // Check for any overlapping label
            boolean hasMatch = false;
            for (String word : foundDescription) {
                if (labels.contains(word)) {
                    hasMatch = true;
                    break;
                }
            }

            if (hasMatch) {
                // Check if a Matches object already exists for this foundItem
                Match existingMatch = null;
                for (Match match : matches) {
                    if (match.getImgPath().equals(((FoundItem) foundItem).getImgPath())) {
                        existingMatch = match;
                        break;
                    }
                }

                if (existingMatch != null) {
                    firestoreService.addLostItemToMatch(existingMatch.getId(), lostItem);
                    getMatchRepo().addLostItem(lostItem, existingMatch);
                } else {
                    Match newMatch = new Match(((FoundItem) foundItem).getImgPath(), foundItem.getTitle());
                    List<LostItem> lostList = new ArrayList<>();
                    lostList.add(lostItem);
                    newMatch.setLostItems(lostList);
                    getMatchRepo().addMatch(newMatch);
                    firestoreService.addMatch(newMatch);
                }
            }
        }

        // Optional callback
        if (onComplete != null) {
            onComplete.run();
        }
    }


    public List<String> convertToList(String description) {
        return Arrays.stream(description.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
