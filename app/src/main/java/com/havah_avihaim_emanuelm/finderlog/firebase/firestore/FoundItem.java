package com.havah_avihaim_emanuelm.finderlog.firebase.firestore;

import java.util.Date;
import java.util.List;

public class FoundItem implements Item {
    private String id;
    private String description;
    private String imgPath;
    private String imgType;
    private String status;
    private Date foundDate;

    public FoundItem() {};
    public FoundItem( String imgPath, String imgType, String description) {
        this.imgPath = imgPath;
        this.imgType = imgType;
//        this.id = TODO: Generate
        this.status = "open";
        this.foundDate = new Date();
        this.description = description;
    }


    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgPath() {
        return imgPath;
    }


    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public Date getFoundDate() {
        return foundDate;
    }


    @Override
    public String getId() {
        return id;
    }

    public String getImgType() {
        return imgType;
    }

}
