package com.havah_avihaim_emanuelm.finderlog.items;

import java.util.Date;

public class LostItem implements Item {
    private String id;
    private String clientName;
    private String clientPhone;
    private String description;
    private String status;
    private String title;
    private Date lostDate;
    private Date reportDate;
    @SuppressWarnings("unused")
    public LostItem() {
    }

    public LostItem(String clientName, String clientPhone, String description,
                    String status, String title, Date lostDate, Date reportDate) {
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.description = description;
        this.status = status;
        this.title = title;
        this.lostDate = lostDate;
        this.reportDate = reportDate;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getLostDate() {
        return lostDate;
    }

    public Date getReportDate() {
        return reportDate;
    }

    @Override
    public String getId() {
        return id;
    }
}
