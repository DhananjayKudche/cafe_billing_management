package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "raw_materials")
public class RawMaterial {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public String unit; // GRAM / ML

    public double currentStock;

    @Override
    public String toString() {
        return name + " (" + unit + ")";
    }
}