package com.kudche.cafebillingmanagement.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kudche.cafebillingmanagement.Models.Product;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    long insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("DELETE FROM products")
    void deleteAll();

    @Query("SELECT * FROM products WHERE id=:id")
    Product getById(int id);

    @Query("SELECT * FROM products WHERE isActive = 1")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getProductById(int id);

    // Used for background operations like recipe setup
    @Query("SELECT * FROM products")
    LiveData<List<Product>> getAll();

    @Query("SELECT * FROM products WHERE id = :id")
    Product getByIdSync(int id);

    @Query("SELECT * FROM products")
    List<Product> getAllSync();
}