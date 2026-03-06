package com.kudche.cafebillingmanagement.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kudche.cafebillingmanagement.Dao.RawMaterialDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.Repository.ProductRepository;

import android.os.Handler;
import android.os.Looper;
import java.util.function.Consumer;
import java.util.concurrent.Executors;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository repository;

    private final RawMaterialDao rawMaterialDao;

    public ProductViewModel(@NonNull Application application) {
        super(application);

        AppDatabase db = AppDatabase.getInstance(application);

        rawMaterialDao = db.rawMaterialDao();
        repository = new ProductRepository(application, rawMaterialDao);
    }

    public LiveData<List<Product>> getProducts() {
        return repository.getProducts();
    }

    public void insertProduct(Product product,
                              List<ProductRawMaterial> materials){
        repository.insertProduct(product, materials);
    }

    public void updateProduct(Product product,
                              List<ProductRawMaterial> materials){
        repository.updateProduct(product, materials);
    }

    public void delete(Product product){
        repository.delete(product);
    }

    // 🔹 ADD THIS
    public Product getProductById(int productId){
        return repository.getProductById(productId);
    }

    // 🔹 ADD THIS
    public List<ProductRawMaterial> getMaterialsByProduct(int productId){
        return repository.getMaterialsByProduct(productId);
    }

//    public ProductRawMaterial getRawMaterialById(int rawMaterialId){
//        return repository.getRawMaterialById(rawMaterialId);
//    }
//
//    public Product getProductById(int id){
//        return repository.getProductById(id);
//    }
//
//    public List<ProductRawMaterial> getMaterialsByProduct(int productId){
//        return repository.getMaterialsByProduct(productId);
//    }

//    public RawMaterial getRawMaterialById(int id){
//        return repository.getRawMaterialById(id);
//    }
    public void getRawMaterialById(int id, Consumer<RawMaterial> callback){

        Executors.newSingleThreadExecutor().execute(() -> {

            RawMaterial raw = repository.getRawMaterialById(id);

            new Handler(Looper.getMainLooper()).post(() ->
                    callback.accept(raw)
            );

        });
    }
}