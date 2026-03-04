package com.kudche.cafebillingmanagement.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kudche.cafebillingmanagement.Models.Recipe;

import java.util.List;

@Dao
public interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE productId = :productId")
    List<Recipe> getRecipeForProduct(int productId);

    @Insert
    void insert(Recipe recipe);
}