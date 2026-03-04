package com.kudche.cafebillingmanagement.Repository;

import android.content.Context;

import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Dao.StockDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.StockEntry;

public class StockRepository {

    private final ProductDao productDao;
    private final StockDao stockDao;

    public StockRepository(Context context) {

        AppDatabase db = AppDatabase.getInstance(context);
        productDao = db.productDao();
        stockDao = db.stockDao();
    }

    public void addStock(int productId, int quantity) {

        new Thread(() -> {

            Product product = productDao.getProductById(productId);

            product.currentStock += quantity;

            productDao.update(product);

            StockEntry entry = new StockEntry();
            entry.productId = productId;
            entry.quantityAdded = quantity;
            entry.addedAt = System.currentTimeMillis();

            stockDao.insertStockEntry(entry);

        }).start();
    }
}