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

    // Accepts FoundItem or LostItem
    public void setItems(List<? extends Item> items) {
        cachedItems.clear();
        items.sort((item1, item2) -> {
            Date date1 = item1.getReportDate();
            Date date2 = item2.getReportDate();
            return date2.compareTo(date1);
        });
        cachedItems.addAll(items);
        needsLoading = false;
    }

    public void addItem(Item item) {
        cachedItems.add(0, item);
    }

    public void removeItemAt(int position) {
        if (position >= 0 && position < cachedItems.size()) {
            cachedItems.remove(position);
        }
    }

    public Item getItemAt(int position) {
        if (position >= 0 && position < cachedItems.size()) {
            return cachedItems.get(position);
        }
        return null;
    }

    public int getSize() {
        return cachedItems.size();
    }

    public List<Item> getCachedItems() {
        return cachedItems;
    }
}
