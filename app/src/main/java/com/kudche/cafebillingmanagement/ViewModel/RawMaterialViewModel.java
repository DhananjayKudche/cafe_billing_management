package com.kudche.cafebillingmanagement.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kudche.cafebillingmanagement.Dao.RawMaterialDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.RawMaterial;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RawMaterialViewModel extends AndroidViewModel {

    private RawMaterialDao dao;
    private ExecutorService executor;

    private LiveData<List<RawMaterial>> allMaterials;

    public RawMaterialViewModel(@NonNull Application application) {
        super(application);

        dao = AppDatabase.getInstance(application).rawMaterialDao();

        executor = Executors.newSingleThreadExecutor();

        allMaterials = dao.getAll();
    }

    public LiveData<List<RawMaterial>> getAll(){
        return allMaterials;
    }

    public void insert(RawMaterial material){
        executor.execute(() -> dao.insert(material));
    }

    public void delete(RawMaterial material){
        executor.execute(() -> dao.delete(material));
    }

    public void update(RawMaterial material){
        executor.execute(() -> dao.update(material));
    }

    public RawMaterial getByIdSync(int id){
        return dao.getByIdSync(id);
    }
}