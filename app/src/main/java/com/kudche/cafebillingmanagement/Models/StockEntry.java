package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stock_entries")
public class StockEntry {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int productId;

    public int quantityAdded;

    public long addedAt;

    public String addedBy;
}