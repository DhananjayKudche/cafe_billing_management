package com.kudche.cafebillingmanagement.Repository;


import android.content.Context;
import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.Utils.StockManager;

import java.util.List;

public class SaleRepository {

    private final AppDatabase db;
    private final ProductDao productDao;
    private final SaleDao saleDao;

    public SaleRepository(Context context) {
        db = AppDatabase.getInstance(context);
        productDao = db.productDao();
        saleDao = db.saleDao();
    }

    public void createSale(List<SaleItem> items) {
        // Run database operations in a single transaction
        new Thread(() -> {
            db.runInTransaction(() -> {
                double total = 0;

                for (SaleItem item : items) {
                    Product product = productDao.getByIdSync(item.productId);
                    if (product == null) continue;

                    // This handles BOTH raw material reduction AND product stock reduction (1x)
                    StockManager.deductStock(db, item.productId, item.quantity);

                    item.priceAtSale = product.price;
                    total += product.price * item.quantity;
                }

                Sale sale = new Sale();
                sale.totalAmount = total;
                sale.paymentType = "CASH";
                sale.createdAt = System.currentTimeMillis();
                sale.createdBy = "admin";

                long saleId = saleDao.insertSale(sale);

                for (SaleItem item : items) {
                    item.saleId = (int) saleId;
                }

                saleDao.insertSaleItems(items);
            });
        }).start();
    }
}