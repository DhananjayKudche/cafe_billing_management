package com.kudche.cafebillingmanagement.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.Repository.RawMaterialRepository;

import java.util.List;

public class RawMaterialViewModel extends AndroidViewModel {

    private final RawMaterialRepository repository;

    public RawMaterialViewModel(@NonNull Application application) {
        super(application);

        repository = new RawMaterialRepository(application);
    }

    public void insert(RawMaterial material) {
        repository.insert(material);
    }

    public LiveData<List<RawMaterial>> getAll() {
        return repository.getAll();
    }
}