package com.havah_avihaim_emanuelm.finderlog.matches;

import com.havah_avihaim_emanuelm.finderlog.items.Item;
import com.havah_avihaim_emanuelm.finderlog.items.LostItem;

import java.util.List;

public class Match {
    private String imgPath;
    private String title;
    private List<LostItem> lostItems;
    private String id;
    @SuppressWarnings("unused")
    public Match() {}
    public Match(String imgPath, String title) {
        this.imgPath = imgPath;
        this.title = title;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void deleteLostItem(Item item) {
        if (item instanceof LostItem) {
            lostItems.remove((LostItem) item);
        }
    }

    public void setLostItems(List<LostItem> lostItems) {
        this.lostItems = lostItems;
    }

    public void addLostItem(LostItem item) {
        lostItems.add(item);
    }

    public List<LostItem> getLostItems() {
        return lostItems;
    }

    public String getImgPath() {
        return imgPath;
    }

    public String getTitle() {
        return title;
    }

}
