package com.havah_avihaim_emanuelm.finderlog.model;

import java.util.Date;

public class FoundItem implements Item{
    private String id;
    private String createdBy;
    private String description;
    private String imgPath;
    private String status;
    private String title;
    private Date foundDate;

    public FoundItem(String id, String createdBy, String description, String imgPath,
                     String status, String title, Date foundDate)  {
        this.id = id;
        this.createdBy = createdBy;
        this.description = description;
        this.imgPath = imgPath;
        this.status = status;
        this.title = title;
        this.foundDate = foundDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getFoundDate() {
        return foundDate;
    }

    public void setFoundDate(Date foundDate) {
        this.foundDate = foundDate;
    }

    @Override
    public String getId() {
        return id;
    }
}
