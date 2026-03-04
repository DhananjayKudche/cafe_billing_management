package com.kudche.cafebillingmanagement.Repository;

import android.content.Context;

import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;

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

    public void createSale(List<SaleItem> items, String paymentType, String user) {

        db.runInTransaction(() -> {

            double total = 0;

            for (SaleItem item : items) {

                Product product = productDao.getProductById(item.productId);

                if (product.currentStock < item.quantity) {
                    throw new RuntimeException("Stock not available");
                }

                product.currentStock -= item.quantity;
                productDao.update(product);

                item.priceAtSale = product.price;

                total += product.price * item.quantity;
            }

            Sale sale = new Sale();
            sale.totalAmount = total;
            sale.paymentType = paymentType;
            sale.createdAt = System.currentTimeMillis();
            sale.createdBy = user;

            long saleId = saleDao.insertSale(sale);

            for (SaleItem item : items) {
                item.saleId = (int) saleId;
            }

            saleDao.insertSaleItems(items);

        });
    }
}