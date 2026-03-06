package com.kudche.cafebillingmanagement.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;

import java.util.List;

@Dao
public interface SaleDao {

    @Insert
    long insertSale(Sale sale);

    @Insert
    void insertSaleItems(List<SaleItem> items);

    @Query("SELECT SUM(totalAmount) FROM sales")
    Double getTotalSales();

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    LiveData<List<Sale>> getAllSales();

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    List<SaleItem> getItemsForSale(int saleId);

    @Query("SELECT * FROM sales WHERE id = :saleId")
    Sale getSaleById(int saleId);

    @Delete
    void deleteSale(Sale sale);

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    void deleteSaleItems(int saleId);

    @Query("SELECT * FROM sales WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    List<Sale> getSalesInRange(long startDate, long endDate);

    @Query("SELECT si.* FROM sale_items si JOIN sales s ON si.saleId = s.id WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<SaleItem> getSaleItemsInRange(long startDate, long endDate);

    // Product Wise Report
    public static class ProductSalesReport {
        public String productName;
        public int quantitySold;
        public double unitPrice;
        public double totalSales;
    }

    @Query("SELECT p.name as productName, SUM(si.quantity) as quantitySold, si.priceAtSale as unitPrice, SUM(si.quantity * si.priceAtSale) as totalSales " +
            "FROM sale_items si " +
            "JOIN products p ON si.productId = p.id " +
            "JOIN sales s ON si.saleId = s.id " +
            "WHERE s.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY si.productId, si.priceAtSale")
    List<ProductSalesReport> getProductWiseSales(long startDate, long endDate);

    // Day Wise Report
    public static class DayWiseSalesReport {
        public String date; // format YYYY-MM-DD
        public int orderCount;
        public int totalItemsSold;
        public double totalSales;
    }

    @Query("SELECT strftime('%Y-%m-%d', datetime(s.createdAt/1000, 'unixepoch', 'localtime')) as date, " +
            "COUNT(DISTINCT s.id) as orderCount, " +
            "SUM(si.quantity) as totalItemsSold, " +
            "SUM(s.totalAmount) / (SELECT COUNT(*) FROM sale_items WHERE saleId = s.id) as totalSales " + 
            "FROM sales s " +
            "JOIN sale_items si ON s.id = si.saleId " +
            "WHERE s.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY date")
    List<DayWiseSalesReport> getDayWiseSales(long startDate, long endDate);
    
    @Query("SELECT strftime('%Y-%m-%d', datetime(createdAt/1000, 'unixepoch', 'localtime')) as date, " +
            "COUNT(id) as orderCount, " +
            "(SELECT SUM(quantity) FROM sale_items si JOIN sales s2 ON si.saleId = s2.id WHERE strftime('%Y-%m-%d', datetime(s2.createdAt/1000, 'unixepoch', 'localtime')) = strftime('%Y-%m-%d', datetime(sales.createdAt/1000, 'unixepoch', 'localtime'))) as totalItemsSold, " +
            "SUM(totalAmount) as totalSales " +
            "FROM sales " +
            "WHERE createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY date")
    List<DayWiseSalesReport> getDayWiseSalesFixed(long startDate, long endDate);
}