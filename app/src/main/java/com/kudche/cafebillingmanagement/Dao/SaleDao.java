package com.kudche.cafebillingmanagement.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;

import java.util.List;

@Dao
public interface SaleDao {

    @Insert
    long insertSale(Sale sale);

    @Insert
    void insertSaleItems(List<SaleItem> items);

    @Query("SELECT SUM(totalAmount) FROM sales")
    Double getTotalSales();
}