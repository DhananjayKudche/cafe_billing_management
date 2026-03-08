package com.kudche.cafebillingmanagement.Database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kudche.cafebillingmanagement.Dao.DayCloseDao;
import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Dao.ProductRawMaterialDao;
import com.kudche.cafebillingmanagement.Dao.RawMaterialDao;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Dao.StockDao;
import com.kudche.cafebillingmanagement.Models.DayClose;
import com.kudche.cafebillingmanagement.Models.DayCloseItem;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.Models.StockEntry;

import java.util.concurrent.Executors;

@Database(
        entities = {
                Product.class,
                Sale.class,
                SaleItem.class,
                StockEntry.class,
                RawMaterial.class,
                ProductRawMaterial.class,
                DayClose.class,
                DayCloseItem.class
        },
        version = 16
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ProductDao productDao();
    public abstract SaleDao saleDao();
    public abstract StockDao stockDao();
    public abstract RawMaterialDao rawMaterialDao();
    public abstract ProductRawMaterialDao productRawMaterialDao();
    public abstract DayCloseDao dayCloseDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "cafe_billing_db"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static final Callback roomCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                // Initial data insertion for Cafe Category
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Tea',10,0,5,0,1,'Cafe Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Special Tea',15,0,5,0,1,'Cafe Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Lemon Tea',15,0,5,0,1,'Cafe Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Black Tea',10,0,5,0,1,'Cafe Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Coffee',20,0,5,0,1,'Cafe Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Black Coffee',15,0,5,0,1,'Cafe Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Bournvita',25,0,5,0,1,'Cafe Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Milk',20,0,5,0,1,'Cafe Category')");

                // Initial data insertion for Juice Category
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Apple Juice',50,0,5,0,1,'Juice Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Pineapple Juice',50,0,5,0,1,'Juice Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Mosambhi Juice',40,0,5,0,1,'Juice Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Dalimb Juice',50,0,5,0,1,'Juice Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Mango Juice',50,0,5,0,1,'Juice Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Chickkoo Juice',40,0,5,0,1,'Juice Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Sitafal Juice',60,0,5,0,1,'Juice Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Strawberrry Juice',60,0,5,0,1,'Juice Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Watermellon Juice',40,0,5,0,1,'Juice Category')");
                db.execSQL("INSERT INTO products (name,price,currentStock,lowStockThreshold,hasRecipe,isActive,category) VALUES ('Mixed Juice',50,0,5,0,1,'Juice Category')");

                // Raw materials
                db.execSQL("INSERT INTO raw_materials (id,name,unit,currentStock) VALUES (1,'Milk','ML',5000)");
                db.execSQL("INSERT INTO raw_materials (id,name,unit,currentStock) VALUES (2,'Tea Powder','GRAM',500)");
                db.execSQL("INSERT INTO raw_materials (id,name,unit,currentStock) VALUES (3,'Coffee Powder','GRAM',500)");
                db.execSQL("INSERT INTO raw_materials (id,name,unit,currentStock) VALUES (4,'Sugar','GRAM',2000)");
            });
        }
    };
}