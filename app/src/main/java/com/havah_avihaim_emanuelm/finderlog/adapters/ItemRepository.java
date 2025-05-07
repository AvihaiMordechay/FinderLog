package com.havah_avihaim_emanuelm.finderlog.adapters;

import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemRepository {
    private final List<Item> cachedItems = new ArrayList<>();
    private boolean isLoaded = false;

    public List<Item> getCachedItems() {
        return cachedItems;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    // Accepts FoundItem or LostItem
    public void setItems(List<? extends Item> items) {
        cachedItems.clear();
        cachedItems.addAll(items);
        isLoaded = true;
    }

    public void addItem(Item item) {
        cachedItems.add(item);
    }

    public void clearCache() {
        cachedItems.clear();
        isLoaded = false;
    }
}
