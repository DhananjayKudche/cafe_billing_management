package com.kudche.cafebillingmanagement.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository repository;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public void addProduct(Product product) {
        repository.insertProduct(product);
    }

    public LiveData<List<Product>> getProducts() {
        return repository.getProducts();
    }

}