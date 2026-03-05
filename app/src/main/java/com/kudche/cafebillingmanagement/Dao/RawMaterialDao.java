package com.kudche.cafebillingmanagement.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kudche.cafebillingmanagement.Models.RawMaterial;

import java.util.List;

@Dao
public interface RawMaterialDao {

    @Insert
    void insert(RawMaterial rawMaterial);

    @Update
    void update(RawMaterial rawMaterial);

    @Query("SELECT * FROM raw_materials")
    LiveData<List<RawMaterial>> getAll();

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    RawMaterial getById(int id);

    // Used when loading spinner in recipe builder
    @Query("SELECT * FROM raw_materials")
    List<RawMaterial> getAllSync();

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    RawMaterial getByIdSync(int id);

    @Delete
    void delete(RawMaterial rawMaterial);

}