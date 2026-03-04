package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public double price;

    public int currentStock;

    public int lowStockThreshold;

    public boolean isActive = true;
}