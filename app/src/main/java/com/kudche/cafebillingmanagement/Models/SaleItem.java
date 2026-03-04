package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sale_items")
public class SaleItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int saleId;

    public int productId;

    public int quantity;

    public double priceAtSale;
}