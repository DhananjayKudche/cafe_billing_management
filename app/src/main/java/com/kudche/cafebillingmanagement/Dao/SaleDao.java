package com.kudche.cafebillingmanagement.Dao;

import androidx.lifecycle.LiveData;
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

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    LiveData<List<Sale>> getAllSales();

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    List<SaleItem> getItemsForSale(int saleId);
}