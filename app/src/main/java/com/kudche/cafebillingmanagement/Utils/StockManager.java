package com.kudche.cafebillingmanagement.Utils;

import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.Models.Recipe;

import java.util.List;

public class StockManager {

    public static boolean deductStock(AppDatabase db, int productId, int quantitySold){

        Product product = db.productDao().getByIdSync(productId);

        // PRODUCT BASED STOCK
        if(!product.hasRecipe){

            if(product.currentStock < quantitySold){
                return false;
            }

            product.currentStock -= quantitySold;

            db.productDao().update(product);

            return true;
        }

        // RECIPE BASED STOCK
        List<Recipe> recipes = db.recipeDao().getRecipeForProduct(productId);

        for(Recipe recipe : recipes){

            RawMaterial material =
                    db.rawMaterialDao().getByIdSync(recipe.rawMaterialId);

            double required = recipe.quantityRequired * quantitySold;

            if(material.currentStock < required){
                return false;
            }
        }

        for(Recipe recipe : recipes){

            RawMaterial material =
                    db.rawMaterialDao().getByIdSync(recipe.rawMaterialId);

            double required = recipe.quantityRequired * quantitySold;

            material.currentStock -= required;

            db.rawMaterialDao().update(material);
        }

        return true;
    }
}