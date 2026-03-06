package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "day_close")
public class DayClose {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long date; // Timestamp for the day

    public String status; // "SUBMITTED", "MATCHED", etc.
    
    public String remarks;
    
    public double totalSalesAmount;
}