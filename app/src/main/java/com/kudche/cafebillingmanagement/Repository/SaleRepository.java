package com.kudche.cafebillingmanagement.Repository;

import android.content.Context;
import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.Utils.StockManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleRepository {

    private final AppDatabase db;
    private final ProductDao productDao;
    private final SaleDao saleDao;

    public interface SaleCallback {
        void onSuccess(Sale sale, List<SaleItem> items);
        void onError(String message);
    }

    public SaleRepository(Context context) {
        db = AppDatabase.getInstance(context);
        productDao = db.productDao();
        saleDao = db.saleDao();
    }

    public void createSale(List<SaleItem> items, boolean isEmergency, SaleCallback callback) {
        new Thread(() -> {
            try {
                db.runInTransaction(() -> {
                    double total = 0;
                    long timestamp = System.currentTimeMillis();
                    String invoiceNo = "INV-" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

                    for (SaleItem item : items) {
                        Product product = productDao.getByIdSync(item.productId);
                        if (product == null) continue;

                        // Deduct stock
                        StockManager.deductStock(db, item.productId, item.quantity);

                        item.priceAtSale = product.price;
                        total += product.price * item.quantity;
                    }

                    Sale sale = new Sale();
                    sale.invoiceNumber = invoiceNo;
                    sale.totalAmount = total;
                    sale.subTotal = total;
                    sale.taxAmount = 0;
                    sale.discountAmount = 0;
                    sale.paymentType = "CASH";
                    sale.createdAt = timestamp;
                    sale.createdBy = "admin";
                    sale.isSynced = false;
                    sale.isEmergencySale = isEmergency;

                    long saleId = saleDao.insertSale(sale);
                    sale.id = (int) saleId;
                    for (SaleItem item : items) {
                        item.saleId = (int) saleId;
                    }
                    saleDao.insertSaleItems(items);
                    
                    if (callback != null) {
                        callback.onSuccess(sale, items);
                    }
                });
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Deletes a sale and REVERTS the stock deducted.
     */
    public void deleteSale(int saleId, Runnable onSuccess) {
        new Thread(() -> {
            db.runInTransaction(() -> {
                Sale sale = saleDao.getSaleById(saleId);
                if (sale == null) return;

                List<SaleItem> items = saleDao.getItemsForSale(saleId);
                for (SaleItem item : items) {
                    // Revert stock: Add back what was sold
                    StockManager.addStock(db, item.productId, item.quantity);
                }

                saleDao.deleteSaleItems(saleId);
                saleDao.deleteSale(sale);
            });
            if (onSuccess != null) onSuccess.run();
        }).start();
    }
}