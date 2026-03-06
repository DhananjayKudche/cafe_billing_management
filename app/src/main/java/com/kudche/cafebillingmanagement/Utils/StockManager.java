package com.kudche.cafebillingmanagement.Utils;

import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;

import java.util.List;

public class StockManager {

    public static boolean isStockAvailable(AppDatabase db, int productId, int quantitySold) {
        Product product = db.productDao().getByIdSync(productId);
        if (product == null) return false;

        if (!product.hasRecipe) {
            return product.currentStock >= quantitySold;
        } else {
            List<ProductRawMaterial> mappings = db.productRawMaterialDao().getByProductSync(productId);
            if (mappings == null || mappings.isEmpty()) {
                // If it's marked as having a recipe but has no ingredients,
                // fallback to checking product stock or return false.
                return product.currentStock >= quantitySold;
            }

            for (ProductRawMaterial map : mappings) {
                RawMaterial material = db.rawMaterialDao().getByIdSync(map.rawMaterialId);
                if (material == null || material.currentStock < (map.quantityRequired * quantitySold)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void deductStock(AppDatabase db, int productId, int quantitySold) {
        Product product = db.productDao().getByIdSync(productId);
        if (product == null) return;

        if (!product.hasRecipe) {
            // Case 1: Direct Product Sale (e.g. Bottled Water, Sandwich)
            product.currentStock -= quantitySold;
            db.productDao().update(product);
        } else {
            // Case 2: Prepared Product (e.g. Coffee)
            List<ProductRawMaterial> mappings = db.productRawMaterialDao().getByProductSync(productId);

            if (mappings != null && !mappings.isEmpty()) {
                for (ProductRawMaterial map : mappings) {
                    RawMaterial material = db.rawMaterialDao().getByIdSync(map.rawMaterialId);
                    if (material != null) {
                        material.currentStock -= (map.quantityRequired * quantitySold);
                        db.rawMaterialDao().update(material);
                    }
                }
                // Optional: Reduce product stock too (if used for "total items sold" tracking)
                product.currentStock -= quantitySold;
                db.productDao().update(product);
            } else {
                // Fallback if recipe is missing
                product.currentStock -= quantitySold;
                db.productDao().update(product);
            }
        }
    }
}