package com.kudche.cafebillingmanagement.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;

import java.util.List;

@Dao
public interface ProductRawMaterialDao {

    @Insert
    void insert(ProductRawMaterial mapping);

    @Query("DELETE FROM product_raw_material WHERE productId = :productId")
    void deleteByProduct(int productId);

    @Query("SELECT * FROM product_raw_material WHERE productId = :productId")
    List<ProductRawMaterial> getByProductSync(int productId);

    @Query("SELECT * FROM product_raw_material WHERE productId = :productId")
    List<ProductRawMaterial> getByProduct(int productId);

    @Query("SELECT * FROM product_raw_material WHERE id = :id LIMIT 1")
    ProductRawMaterial getById(int id);

    @Query("SELECT * FROM product_raw_material")
    List<ProductRawMaterial> getAllSync();
}