package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "day_close_items")
public class DayCloseItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int dayCloseId;
    
    public int rawMaterialId;

    public String materialName;

    public double expectedStock; // Based on sales logic
    public double actualStock;   // Entered by worker
    public double variance;      // actual - expected
}