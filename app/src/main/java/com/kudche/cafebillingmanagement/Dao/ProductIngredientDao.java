package com.kudche.cafebillingmanagement.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kudche.cafebillingmanagement.Models.ProductIngredient;

import java.util.List;

@Dao
public interface ProductIngredientDao {

    @Query("SELECT * FROM product_ingredients WHERE productId = :productId")
    List<ProductIngredient> getIngredientsForProduct(int productId);

    @Insert
    void insert(ProductIngredient ingredient);
}