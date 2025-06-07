package com.havah_avihaim_emanuelm.finderlog.matches;

import static com.havah_avihaim_emanuelm.finderlog.activities.MainActivity.CHANNEL_ID;
import static com.havah_avihaim_emanuelm.finderlog.repositories.Repositories.getFoundRepo;
import static com.havah_avihaim_emanuelm.finderlog.repositories.Repositories.getLostRepo;
import static com.havah_avihaim_emanuelm.finderlog.repositories.Repositories.getMatchRepo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.firebase.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.items.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.items.Item;
import com.havah_avihaim_emanuelm.finderlog.items.LostItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.MachineLearningService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class MatchAlgorithm {
    private static final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
    private final Context context;
    private final FirestoreService firestoreService = FirestoreService.getSharedInstance();
    private String mimeType;
    private Runnable onComplete;
    private String imgTitle;

    public MatchAlgorithm(Context context, String mimeType, Runnable onComplete, String imgTitle) {
        this.context = context;
        this.mimeType = mimeType;
        this.onComplete = onComplete;
        this.imgTitle = imgTitle;

        registerReceiver();
    }

    public MatchAlgorithm(Context context) {
        this.context = context;
    }

    // Registers broadcast receiver to listen for machine learning service results
    // Sets up intent filter for image analysis completion events
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(MachineLearningService.ACTION_ANALYZE_IMAGE);
        ContextCompat.registerReceiver(context, resultReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    // Starts matching process for new uploaded images against existing lost items
    // Searches for matches based on detected labels and creates Match objects
    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String imageUri = intent.getStringExtra(MachineLearningService.EXTRA_IMAGE_URI);
            String labels = intent.getStringExtra(MachineLearningService.EXTRA_LABELS);

            if (imageUri != null) {
                FoundItem foundItem = new FoundItem(imgTitle, imageUri, mimeType, labels);
                firestoreService.addItem(foundItem, item -> {
                    getFoundRepo().addItem(item);
                    if (labels == null) return;
                    new Thread(() -> startMatchingThread(convertToList(labels), imageUri)).start();
                });
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

    // Starts matching process for new uploaded images against existing lost items
    // Searches for matches based on detected labels and creates Match objects
    private void startMatchingThread(List<String> labels, String imageUri) {
        List<Item> lostItems = getLostRepo().getCachedItems();
        List<LostItem> matchLostItems = new ArrayList<>();
        Match match = new Match(imageUri, this.imgTitle);
        boolean hasMatch = false;
        int matchesCount = 0;

        Calendar startDate = getStartDate();
        for (Item item : lostItems) {
            // It will always be a LostItem, but the compiler doesn't know that
            if (!(item instanceof LostItem)) continue;

            LostItem lostItem = (LostItem) item;
            if (startDate != null && lostItem.getLostDate().before(startDate.getTime())) continue;

            List<String> itemDescription = convertToList(item.getDescription());
            for (String word : itemDescription) {
                if (labels.contains(word)) {
                    matchLostItems.add((LostItem) item);
                    hasMatch = true;
                    matchesCount++;
                    break;
                }
            }
        }
        match.setLostItems(matchLostItems);
        if (hasMatch) {
            firestoreService.addMatch(match);
            getMatchRepo().addMatch(match);
            MatchNotification("Match Found", matchesCount + " matches found for " + this.imgTitle + " image");
        }
    }

    // Starts matching process for new lost item reports against existing found items
    // Searches through found items to find matches with the new lost item report
    public void startMatchingThread(List<String> labels, LostItem lostItem) {
        List<Item> foundItems = getFoundRepo().getCachedItems();
        List<Match> matches = getMatchRepo().getMatches();
        int matchesCount = 0;

        Calendar startDate = getStartDate();
        if (startDate != null && lostItem.getLostDate().before(startDate.getTime())) {
            Log.d("lost", "Lost item skipped due to old date: " + lostItem.getTitle());
            return;
        }

        for (Item foundItem : foundItems) {
            if (!(foundItem instanceof FoundItem)) continue;

            FoundItem fi = (FoundItem) foundItem;
            if (startDate != null && fi.getFoundDate().before(startDate.getTime())) continue;

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
                matchesCount++;
            }
        }
        if (matchesCount > 0) {
            MatchNotification("Match Found", matchesCount + " matches found for report " + lostItem.getTitle());
        }
        // Optional callback
        if (onComplete != null) {
            onComplete.run();
        }
    }

    // Displays notification when matches are found
    // Checks user preferences before showing notification
    private void MatchNotification(String title, String message) {

        boolean notificationsEnabled = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("notifications_enabled", false);
        if (!notificationsEnabled) return;

        int notifyId = (int) System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.our_alert)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManagerCompat.from(context).notify(notifyId, builder.build());
    }

    // Converts comma-separated string into list of trimmed strings
    // Filters out empty strings from the result
    public List<String> convertToList(String description) {
        return Arrays.stream(description.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // Gets the start date for filtering items based on user's selected time range
    // Returns calendar object set to beginning of selected period (month/3months/year ago)
    private Calendar getStartDate() {
        int selectedRange = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getInt("selected_range", 0); // 0 = month, 1 = 3 months, 2 = year

        Calendar cal = Calendar.getInstance();
        switch (selectedRange) {
            case 0:
                cal.add(Calendar.MONTH, -1);
                break;
            case 1:
                cal.add(Calendar.MONTH, -3);
                break;
            case 2:
                cal.add(Calendar.YEAR, -1);
                break;
            default:
                return null;
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

}
