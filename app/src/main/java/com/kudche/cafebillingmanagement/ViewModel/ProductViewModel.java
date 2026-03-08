package com.kudche.cafebillingmanagement.ViewModel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kudche.cafebillingmanagement.Dao.RawMaterialDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.Repository.ProductRepository;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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

    public LiveData<List<Product>> getProductsByCategory(String category) {
        return repository.getProductsByCategory(category);
    }

    public void insertProduct(Product product, List<ProductRawMaterial> materials){
        repository.insertProduct(product, materials);
    }

    public void updateProduct(Product product, List<ProductRawMaterial> materials){
        repository.updateProduct(product, materials);
    }

    public void delete(Product product){
        repository.delete(product);
    }

    public Product getProductById(int productId){
        return repository.getProductById(productId);
    }

    public List<ProductRawMaterial> getMaterialsByProduct(int productId){
        return repository.getMaterialsByProduct(productId);
    }

    public void getRawMaterialById(int id, Consumer<RawMaterial> callback){
        Executors.newSingleThreadExecutor().execute(() -> {
            RawMaterial raw = repository.getRawMaterialById(id);
            new Handler(Looper.getMainLooper()).post(() ->
                    callback.accept(raw)
            );
        });
    }
}