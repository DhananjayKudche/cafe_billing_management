package com.kudche.cafebillingmanagement.ViewModel;

import androidx.lifecycle.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.kudche.cafebillingmanagement.Repository.StockRepository;

public class StockViewModel extends AndroidViewModel {

    private final StockRepository repository;

    public StockViewModel(@NonNull Application application) {
        super(application);
        repository = new StockRepository(application);
    }

    public void addStock(int productId, int quantity) {
        repository.addStock(productId, quantity);
    }
}