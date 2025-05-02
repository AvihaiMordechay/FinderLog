package com.havah_avihaim_emanuelm.finderlog.model;

import java.util.Date;

public class LostItem implements Item{
    private String id;

    private String clientName;
    private String clientPhone;
    private String description;
    private String status;
    private String title;
    private Date lostDate;
    private Date reportDate;

    public LostItem(String id, String clientName, String clientPhone, String description,
                    String status, String title, Date lostDate, Date reportDate) {
        this.id = id;
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

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Date getLostDate() {
        return lostDate;
    }

    public void setLostDate(Date lostDate) {
        this.lostDate = lostDate;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    @Override
    public String getId() {
        return id;
    }
}
