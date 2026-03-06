package com.kudche.cafebillingmanagement.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.kudche.cafebillingmanagement.Models.DayClose;
import com.kudche.cafebillingmanagement.Models.DayCloseItem;

import java.util.List;

@Dao
public interface DayCloseDao {

    @Insert
    long insert(DayClose dayClose);

    @Insert
    void insertItems(List<DayCloseItem> items);

    @Query("SELECT * FROM day_close ORDER BY date DESC")
    LiveData<List<DayClose>> getAll();

    @Query("SELECT * FROM day_close_items WHERE dayCloseId = :dayCloseId")
    List<DayCloseItem> getItemsForDayClose(int dayCloseId);

    @Query("SELECT MAX(date) FROM day_close")
    Long getLastDayCloseDate();
}