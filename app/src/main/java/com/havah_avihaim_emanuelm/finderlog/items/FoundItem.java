package com.havah_avihaim_emanuelm.finderlog.items;

import java.util.Date;

public class FoundItem implements Item {
    private String id;
    private String description;
    private String imgPath;
    private String imgType;
    private String status;
    private String title;
    private Date foundDate;

    public FoundItem() {};
    public FoundItem(String title, String imgPath, String imgType, String description) {
        this.title = title;
        this.imgPath = imgPath;
        this.imgType = imgType;
        this.status = "open";
        this.foundDate = new Date();
        this.description = description;
    }


    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setId(String id) {
        this.id = id;
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

    @Override
    public String getTitle() {
        return title;
    }

    public Date getReportDate() {
        return foundDate;
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
