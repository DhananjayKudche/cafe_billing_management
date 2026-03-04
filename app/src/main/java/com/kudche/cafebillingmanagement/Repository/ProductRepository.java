package com.kudche.cafebillingmanagement.Repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;

import java.util.List;

public class ProductRepository {

    private final ProductDao productDao;

    public ProductRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        productDao = db.productDao();
    }

    public void insertProduct(Product product) {

        new Thread(() -> {

            long id = productDao.insert(product);

            Log.d("DB_TEST", "Product inserted with ID: " + id);

        }).start();
    }

    public LiveData<List<Product>> getProducts() {
        return productDao.getAllProducts();
    }

}