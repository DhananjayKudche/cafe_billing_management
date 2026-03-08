package com.kudche.cafebillingmanagement.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kudche.cafebillingmanagement.Models.BulkOrder;

import java.util.List;

@Dao
public interface BulkOrderDao {
    @Insert
    long insert(BulkOrder bulkOrder);

    @Update
    void update(BulkOrder bulkOrder);

    @Delete
    void delete(BulkOrder bulkOrder);

    @Query("SELECT * FROM bulk_orders ORDER BY date DESC")
    LiveData<List<BulkOrder>> getAllBulkOrders();

    @Query("SELECT * FROM bulk_orders WHERE rawMaterialId = :rawId AND remainingInBulk > 0")
    List<BulkOrder> getActiveBulkOrdersByMaterial(int rawId);
    
    @Query("SELECT * FROM bulk_orders WHERE id = :id")
    BulkOrder getById(int id);
}