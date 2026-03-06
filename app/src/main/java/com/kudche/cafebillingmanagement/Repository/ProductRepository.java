package com.kudche.cafebillingmanagement.Repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Dao.ProductRawMaterialDao;
import com.kudche.cafebillingmanagement.Dao.RawMaterialDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {

    private final ProductDao productDao;
    private final ProductRawMaterialDao mappingDao;

    private final RawMaterialDao rawMaterialDao;
    private final LiveData<List<Product>> products;
    private final ExecutorService executor;

    public ProductRepository(Application app, RawMaterialDao rawMaterialDao){
        this.rawMaterialDao = rawMaterialDao;

        AppDatabase db = AppDatabase.getInstance(app);

        productDao = db.productDao();
        mappingDao = db.productRawMaterialDao();
        products = productDao.getAll();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Product>> getProducts(){
        return products;
    }

    public void insertProduct(Product product,
                              List<ProductRawMaterial> materials){

        executor.execute(() -> {

            long productId = productDao.insert(product);

            for(ProductRawMaterial m : materials){
                m.productId = (int) productId;
                mappingDao.insert(m);
            }
        });
    }

    public void updateProduct(Product product,
                              List<ProductRawMaterial> materials){

        executor.execute(() -> {

            productDao.update(product);

            mappingDao.deleteByProduct(product.id);

            for(ProductRawMaterial m : materials){

                m.productId = product.id;

                mappingDao.insert(m);
            }
        });
    }

    public void delete(Product product){
        executor.execute(() -> productDao.delete(product));
    }

    public void deleteAllProducts(){
        executor.execute(() -> {
            productDao.deleteAll();
        });
    }

//    public Product getProductById(int id){
//        return productDao.getProductById(id);
//    }
//
//    public List<ProductRawMaterial> getMaterialsByProduct(int productId){
//        return mappingDao.getByProduct(productId);
//    }
//
//    public ProductRawMaterial getRawMaterialById(int id){
//        return mappingDao.getById(id);
//    }

    public Product getProductById(int id){
        return productDao.getById(id);
    }

    public List<ProductRawMaterial> getMaterialsByProduct(int productId){
        return mappingDao.getByProduct(productId);
    }

    public RawMaterial getRawMaterialById(int id){
        return rawMaterialDao.getById(id);
    }


}