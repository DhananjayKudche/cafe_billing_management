package com.kudche.cafebillingmanagement.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportGenerator {

    public static File generateDailyReport(Context context, long startDate, long endDate) throws IOException {
        AppDatabase db = AppDatabase.getInstance(context);
        SaleDao saleDao = db.saleDao();
        List<Sale> sales = saleDao.getSalesInRange(startDate, endDate);

        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String dateStr = displayFormat.format(new Date(startDate)) + " - " + displayFormat.format(new Date(endDate));
        if (startDate == getStartOfDay(startDate) && endDate == (getStartOfDay(startDate) + (24 * 60 * 60 * 1000) - 1)) {
            dateStr = displayFormat.format(new Date(startDate));
        }

        String fileName = "Sales_Report_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getCacheDir(), fileName);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int y = 50;
        
        // Header
        paint.setTextSize(22f);
        paint.setFakeBoldText(true);
        canvas.drawText(Constants.CAFE_NAME, 50, y, paint);
        
        y += 25;
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        canvas.drawText(Constants.CAFE_ADDRESS + " | Contact: " + Constants.CAFE_CONTACT, 50, y, paint);

        y += 35;
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        canvas.drawText("Sales History Report", 50, y, paint);
        y += 20;
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        canvas.drawText("Period: " + dateStr, 50, y, paint);

        y += 30;
        paint.setFakeBoldText(true);
        canvas.drawText("Detailed Order Breakdown", 50, y, paint);
        y += 8;
        canvas.drawLine(50, y, 550, y, paint);
        y += 25;
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        double totalSales = 0;

        for (Sale sale : sales) {
            if (y > 720) {
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }

            paint.setFakeBoldText(true);
            paint.setTextSize(11f);
            String orderType = sale.isParcel ? "PARCEL" : "DINE-IN";
            String inv = sale.invoiceNumber != null ? sale.invoiceNumber : "ORD" + sale.id;
            canvas.drawText(inv + " (" + orderType + ") at " + timeFormat.format(new Date(sale.createdAt)), 50, y, paint);
            
            y += 18;
            paint.setFakeBoldText(false);
            paint.setTextSize(10f);
            
            // Item headers
            canvas.drawText("Item Name", 70, y, paint);
            canvas.drawText("Qty", 320, y, paint);
            canvas.drawText("Price", 400, y, paint);
            canvas.drawText("Amount", 480, y, paint);
            y += 5;
            paint.setStrokeWidth(0.5f);
            canvas.drawLine(70, y, 530, y, paint);
            y += 15;

            List<SaleItem> items = saleDao.getItemsForSale(sale.id);
            for (SaleItem item : items) {
                if (y > 800) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }
                Product p = db.productDao().getByIdSync(item.productId);
                String name = p != null ? p.name : "Unknown Product";
                
                canvas.drawText(name, 70, y, paint);
                canvas.drawText(String.valueOf(item.quantity), 320, y, paint);
                canvas.drawText(String.format(Locale.getDefault(), "%.0f", item.priceAtSale), 400, y, paint);
                canvas.drawText(String.format(Locale.getDefault(), "%.0f", item.quantity * item.priceAtSale), 480, y, paint);
                y += 15;
            }

            paint.setFakeBoldText(true);
            canvas.drawText("Order Total: Rs. " + String.format(Locale.getDefault(), "%.2f", sale.totalAmount), 410, y, paint);
            totalSales += sale.totalAmount;
            
            y += 30;
            paint.setStrokeWidth(1f);
            canvas.drawLine(50, y-15, 550, y-15, paint);
            y += 10;
        }

        if (y > 780) {
            document.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            y = 50;
        }

        y += 20;
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        canvas.drawText("GRAND TOTAL SALES: Rs. " + String.format(Locale.getDefault(), "%.2f", totalSales), 50, y, paint);

        document.finishPage(page);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
        } finally {
            document.close();
        }

        return file;
    }

    public static File generateDailyReport(Context context, long dateTimestamp) throws IOException {
        long startOfDay = getStartOfDay(dateTimestamp);
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1;
        return generateDailyReport(context, startOfDay, endOfDay);
    }

    private static long getStartOfDay(long timestamp) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}