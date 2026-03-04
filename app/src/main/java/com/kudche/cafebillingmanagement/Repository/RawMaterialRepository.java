package com.kudche.cafebillingmanagement.Repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.kudche.cafebillingmanagement.Dao.RawMaterialDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.RawMaterial;

import java.util.List;

public class RawMaterialRepository {

    private final RawMaterialDao rawMaterialDao;

    public RawMaterialRepository(Context context) {

        AppDatabase db = AppDatabase.getInstance(context);
        rawMaterialDao = db.rawMaterialDao();
    }

    public void insert(RawMaterial material) {

        new Thread(() -> rawMaterialDao.insert(material)).start();
    }

    public LiveData<List<RawMaterial>> getAll() {

        return rawMaterialDao.getAll();
    }
}