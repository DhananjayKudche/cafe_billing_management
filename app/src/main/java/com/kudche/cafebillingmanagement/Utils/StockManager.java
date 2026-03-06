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
        adjustStock(db, productId, quantitySold, true);
    }

    public static void addStock(AppDatabase db, int productId, int quantitySold) {
        adjustStock(db, productId, quantitySold, false);
    }

    private static void adjustStock(AppDatabase db, int productId, int quantity, boolean isDeduction) {
        Product product = db.productDao().getByIdSync(productId);
        if (product == null) return;

        int multiplier = isDeduction ? 1 : -1;

        if (!product.hasRecipe) {
            product.currentStock -= (quantity * multiplier);
            db.productDao().update(product);
        } else {
            List<ProductRawMaterial> mappings = db.productRawMaterialDao().getByProductSync(productId);

            if (mappings != null && !mappings.isEmpty()) {
                for (ProductRawMaterial map : mappings) {
                    RawMaterial material = db.rawMaterialDao().getByIdSync(map.rawMaterialId);
                    if (material != null) {
                        material.currentStock -= (map.quantityRequired * quantity * multiplier);
                        db.rawMaterialDao().update(material);
                    }
                }
                product.currentStock -= (quantity * multiplier);
                db.productDao().update(product);
            } else {
                product.currentStock -= (quantity * multiplier);
                db.productDao().update(product);
            }
        }
    }
}