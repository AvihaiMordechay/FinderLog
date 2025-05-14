    package com.havah_avihaim_emanuelm.finderlog.firebase.firestore;

    import android.util.Log;

    import com.google.firebase.firestore.*;

    import java.util.*;

    public class FirestoreService {
        private final FirebaseFirestore db = FirebaseFirestore.getInstance();

        // TODO: ADD THE ITEM TO THE HAVA'S LISTS!!!!!!!!
        public void addItem(Item item) {
            String collection = getCollectionName(item);
            db.collection(collection)
                    .add(item)
                    .addOnSuccessListener(doc -> Log.d("Firestore", "Item added to " + collection + ": " + doc.getId()))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding item", e));
        }

        public void getItems(Class<? extends Item> clazz, Listener<List<? extends Item>> callback) {
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
    }
