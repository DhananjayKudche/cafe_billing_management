package com.kudche.cafebillingmanagement.Dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.kudche.cafebillingmanagement.Models.StockEntry;

@Dao
public interface StockDao {

    @Insert
    void insertStockEntry(StockEntry entry);
}