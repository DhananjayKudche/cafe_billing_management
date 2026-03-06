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

    public boolean hasRecipe = false;

    public boolean isActive = true;

    public String imagePath; // Stores the URI or path of the product image

    @Override
    public String toString() {
        return name;
    }
}