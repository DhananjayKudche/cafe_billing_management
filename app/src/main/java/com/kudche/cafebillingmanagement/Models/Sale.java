package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales")
public class Sale {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String invoiceNumber;

    public double totalAmount;

    public double subTotal;
    public double taxAmount;
    public double discountAmount;

    public String paymentType;

    public long createdAt;

    public String createdBy;

    public boolean isSynced = false;
    public String syncBatchId;

    // Flag to indicate if any item in this sale was sold without sufficient stock recorded
    public boolean isEmergencySale = false;

    // Flag to indicate if this is a parcel order (payment pending delivery)
    public boolean isParcel = false;
}