package com.havah_avihaim_emanuelm.finderlog.items;

import java.util.Date;

public interface Item {
    String getId();
    String getTitle();
    String getDescription();
    String getStatus();
    Date getReportDate();
    void setId(String id);
}
