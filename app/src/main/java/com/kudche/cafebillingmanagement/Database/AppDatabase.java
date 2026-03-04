package com.kudche.cafebillingmanagement.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Dao.StockDao;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.Models.StockEntry;

@Database(
        entities = {Product.class, Sale.class, SaleItem.class, StockEntry.class},
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ProductDao productDao();
    public abstract SaleDao saleDao();
    public abstract StockDao stockDao();

    public static synchronized AppDatabase getInstance(Context context) {

        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "cafe_billing_db"
            ).fallbackToDestructiveMigration().build();
        }

        return instance;
    }
}