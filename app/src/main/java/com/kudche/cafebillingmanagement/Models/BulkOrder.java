package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bulk_orders")
public class BulkOrder {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int rawMaterialId;
    public String rawMaterialName;
    public double totalQuantity; // Total quantity bought in bulk (e.g., 10 KG)
    public double remainingInBulk; // Quantity still in owner's possession (e.g., 9 KG)
    public String unit;
    public long date;
    public String supplierName;
}