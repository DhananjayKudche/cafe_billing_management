package com.kudche.cafebillingmanagement.Database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Dao.ProductRawMaterialDao;
import com.kudche.cafebillingmanagement.Dao.RawMaterialDao;
import com.kudche.cafebillingmanagement.Dao.RecipeDao;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Dao.StockDao;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.Models.Recipe;
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
                Recipe.class,
                ProductRawMaterial.class
        },
        version = 8
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ProductDao productDao();
    public abstract SaleDao saleDao();
    public abstract StockDao stockDao();
    public abstract RawMaterialDao rawMaterialDao();
    public abstract RecipeDao recipeDao();
    public abstract ProductRawMaterialDao productRawMaterialDao();
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

                // PRODUCTS
                db.execSQL("INSERT INTO products (id,name,price,currentStock,lowStockThreshold,hasRecipe,isActive) VALUES (1,'Chai',10,0,5,1,1)");
                db.execSQL("INSERT INTO products (id,name,price,currentStock,lowStockThreshold,hasRecipe,isActive) VALUES (2,'Coffee',20,0,5,1,1)");
                db.execSQL("INSERT INTO products (id,name,price,currentStock,lowStockThreshold,hasRecipe,isActive) VALUES (3,'Sandwich',40,50,5,0,1)");

                // RAW MATERIALS
                db.execSQL("INSERT INTO raw_materials (id,name,unit,currentStock) VALUES (1,'Milk','ML',5000)");
                db.execSQL("INSERT INTO raw_materials (id,name,unit,currentStock) VALUES (2,'Tea Powder','GRAM',500)");
                db.execSQL("INSERT INTO raw_materials (id,name,unit,currentStock) VALUES (3,'Coffee Powder','GRAM',500)");
                db.execSQL("INSERT INTO raw_materials (id,name,unit,currentStock) VALUES (4,'Sugar','GRAM',2000)");

                // CHAI RECIPE
                db.execSQL("INSERT INTO recipes (productId,rawMaterialId,quantityRequired) VALUES (1,1,200)");
                db.execSQL("INSERT INTO recipes (productId,rawMaterialId,quantityRequired) VALUES (1,2,5)");
                db.execSQL("INSERT INTO recipes (productId,rawMaterialId,quantityRequired) VALUES (1,4,10)");

                // COFFEE RECIPE
                db.execSQL("INSERT INTO recipes (productId,rawMaterialId,quantityRequired) VALUES (2,1,200)");
                db.execSQL("INSERT INTO recipes (productId,rawMaterialId,quantityRequired) VALUES (2,3,5)");
                db.execSQL("INSERT INTO recipes (productId,rawMaterialId,quantityRequired) VALUES (2,4,8)");
            });
        }
    };
}