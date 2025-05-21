package com.havah_avihaim_emanuelm.finderlog.firebase.firestore;

import java.util.Date;

public interface Item {
    String getId();
    String getTitle();
    String getDescription();
    String getStatus();
    Date getReportDate();
    void setId(String id);
}
