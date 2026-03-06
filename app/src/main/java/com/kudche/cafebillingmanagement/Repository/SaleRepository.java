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

    public SaleRepository(Context context) {
        db = AppDatabase.getInstance(context);
        productDao = db.productDao();
        saleDao = db.saleDao();
    }

    public void createSale(List<SaleItem> items, boolean isEmergency) {

        new Thread(() -> {

            db.runInTransaction(() -> {

                double total = 0;
                long timestamp = System.currentTimeMillis();

                String invoiceNo = "INV-" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

                for (SaleItem item : items) {
                    Product product = productDao.getByIdSync(item.productId);
                    if (product == null) continue;

                    // Deduct stock (Will go negative if isEmergency is true)
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
                sale.isEmergencySale = isEmergency; // Mark if stock was bypassed

                long saleId = saleDao.insertSale(sale);

                for (SaleItem item : items) {
                    item.saleId = (int) saleId;
                }

                saleDao.insertSaleItems(items);

            });

        }).start();
    }
}