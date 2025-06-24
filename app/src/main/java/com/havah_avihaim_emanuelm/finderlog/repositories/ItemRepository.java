package com.havah_avihaim_emanuelm.finderlog.repositories;

import com.havah_avihaim_emanuelm.finderlog.items.Item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemRepository {
    private final List<Item> cachedItems = new ArrayList<>();
    private boolean needsLoading = true;

    public boolean needsLoading() {
        return needsLoading;
    }

    // Sets the cached items list, sorted by report date (newest first), and marks loading as complete.
    public void setItems(List<? extends Item> items) {
        cachedItems.clear();
        if(items != null) {
            items.sort((item1, item2) -> {
                Date date1 = item1.getReportDate();
                Date date2 = item2.getReportDate();
                return date2.compareTo(date1);
            });
            cachedItems.addAll(items);
        }
        needsLoading = false;
    }

    // Adds a new item to the beginning of the cached list.
    public void addItem(Item item) {
        cachedItems.add(0, item);
    }

    // Removes an item from the specified position if it is within bounds.
    public void removeItemAt(int position) {
        if (position >= 0 && position < cachedItems.size()) {
            cachedItems.remove(position);
        }
    }

    // Retrieves the item at the specified position, or null if out of bounds.
    public Item getItemAt(int position) {
        if (position >= 0 && position < cachedItems.size()) {
            return cachedItems.get(position);
        }
        return null;
    }

    // Returns the number of cached items.
    public int getSize() {
        return cachedItems.size();
    }

    // Returns the full list of cached items.
    public List<Item> getCachedItems() {
        return cachedItems;
    }
}
