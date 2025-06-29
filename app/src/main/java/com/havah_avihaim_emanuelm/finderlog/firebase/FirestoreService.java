package com.havah_avihaim_emanuelm.finderlog.firebase;

import android.util.Log;

import com.google.firebase.firestore.*;
import com.havah_avihaim_emanuelm.finderlog.items.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.items.Item;
import com.havah_avihaim_emanuelm.finderlog.items.LostItem;
import com.havah_avihaim_emanuelm.finderlog.matches.Match;

import java.util.*;

public class FirestoreService {
    private static FirestoreService instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirestoreService() {
    }

    public static synchronized FirestoreService getSharedInstance() {
        if (instance == null) {
            instance = new FirestoreService();
        }
        return instance;
    }

    /**
     * Items:
     */
    // Adds a new item (lost or found) to Firestore and returns it via callback with its generated ID.
    public void addItem(Item item, ResultCallback<Item> callback) {
        String collection = getCollectionName(item);
        db.collection(collection)
                .add(item)
                .addOnSuccessListener(docRef -> {
                    String id = docRef.getId();
                    item.setId(id);
                    docRef.update("id", id);
                    Log.d("Firestore", "Item added to " + collection + ": " + id);
                    callback.onResult(item);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding item", e));
    }

    // Retrieves all items of the specified class (LostItem or FoundItem) from Firestore and returns them via callback.
    public void getItems(Class<? extends Item> clazz, ResultCallback<List<? extends Item>> callback) {
        String collection = getCollectionName(clazz);
        db.collection(collection)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<Item> items = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        items.add(doc.toObject(clazz));
                    }
                    callback.onResult(items);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error getting items", e));
    }

    // Deletes an item by ID from the appropriate collection (lost or found) in Firestore.
    public void deleteItem(Class<? extends Item> clazz, String id) {
        String collection = getCollectionName(clazz);
        db.collection(collection).document(id).delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Item deleted from " + collection))
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting item", e));
    }

    private String getCollectionName(Item item) {
        if (item instanceof FoundItem) return "found_items";
        if (item instanceof LostItem) return "lost_items";
        throw new IllegalArgumentException("Unknown item type: " + item.getClass().getSimpleName());
    }

    private String getCollectionName(Class<? extends Item> clazz) {
        if (clazz == FoundItem.class) return "found_items";
        if (clazz == LostItem.class) return "lost_items";
        throw new IllegalArgumentException("Unknown class type: " + clazz.getSimpleName());
    }

    /**
     * Matches:
     */
    // Adds a match to Firestore and assigns the generated ID to the match object.
    public void addMatch(Match match) {
        db.collection("matches")
                .add(match)
                .addOnSuccessListener(docRef -> {
                    String id = docRef.getId();
                    match.setId(id);
                    docRef.update("id", id);
                    Log.d("Firestore", "Matches added: " + id);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding match", e));
    }

    // Adds a LostItem to an existing match by match ID and updates the match document in Firestore.
    public void addLostItemToMatch(String matchId, LostItem newLostItem) {
        DocumentReference matchRef = db.collection("matches").document(matchId);

        matchRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Match match = snapshot.toObject(Match.class);
                        if (match != null) {
                            List<LostItem> currentItems = match.getLostItems();
                            currentItems.add(newLostItem);
                            matchRef.update("lostItems", currentItems)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "LostItem added to match " + matchId))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error updating match", e));
                        }
                    } else {
                        Log.e("Firestore", "Match not found with id: " + matchId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching match", e));
    }

    // Retrieves all matches from Firestore and returns them via callback.
    public void getAllMatches(ResultCallback<List<Match>> callback) {
        db.collection("matches")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Match> matchesList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Match match = doc.toObject(Match.class);
                        matchesList.add(match);
                    }
                    callback.onResult(matchesList);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error getting matches", e));
    }

    // Removes a specified LostItem from all matches in which it appears and updates the match documents accordingly.
    public void deleteLostItemFromMatches(LostItem lostItem) {
        db.collection("matches")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        DocumentReference matchRef = doc.getReference();
                        Match match = doc.toObject(Match.class);

                        if (match != null && match.getLostItems() != null) {
                            List<LostItem> currentLostItems = match.getLostItems();
                            List<LostItem> updatedItems = new ArrayList<>();
                            boolean found = false;

                            for (LostItem item : currentLostItems) {
                                if (item.getId() != null && item.getId().equals(lostItem.getId())) {
                                    found = true;
                                } else {
                                    updatedItems.add(item);
                                }
                            }

                            if (found) {
                                if (updatedItems.isEmpty()) {
                                    // Delete the entire match document if no lostItems left
                                    matchRef.delete()
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Match deleted: " + doc.getId()))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error deleting match", e));
                                } else {
                                    // Just update the lostItems field
                                    matchRef.update("lostItems", updatedItems)
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "LostItem removed from match " + doc.getId()))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error removing lostItem from match", e));
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error getting matches", e));
    }

}
