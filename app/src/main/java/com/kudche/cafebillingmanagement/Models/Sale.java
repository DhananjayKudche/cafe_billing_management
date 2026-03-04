package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales")
public class Sale {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public double totalAmount;

    public String paymentType;

    public long createdAt;

    public String createdBy;

    public String printStatus = "PENDING";
}