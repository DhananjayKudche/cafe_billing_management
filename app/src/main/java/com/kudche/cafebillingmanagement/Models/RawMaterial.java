package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "raw_materials")
public class RawMaterial {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public String unit; // KG, LITER, QUANTITY (Base units stored in DB)

    public double currentStock; // Always stored in base units (KG, LITER, PCS)

    @Override
    public String toString() {
        return name;
    }
}