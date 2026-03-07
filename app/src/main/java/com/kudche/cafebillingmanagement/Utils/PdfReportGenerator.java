package com.kudche.cafebillingmanagement.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Models.Sale;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportGenerator {

    public static File generateDailyReport(Context context, long dateTimestamp) throws IOException {
        long startOfDay = getStartOfDay(dateTimestamp);
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1;

        SaleDao saleDao = AppDatabase.getInstance(context).saleDao();
        List<Sale> sales = saleDao.getSalesInRange(startOfDay, endOfDay);

        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(dateTimestamp));
        String fileName = "report_" + dateStr + ".pdf";
        File file = new File(context.getCacheDir(), fileName);

        PdfDocument document = new PdfDocument();
        // A4 size: 595 x 842 points
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int y = 50;
        
        // Header
        paint.setTextSize(20f);
        paint.setFakeBoldText(true);
        canvas.drawText(Constants.CAFE_NAME, 50, y, paint);
        
        y += 30;
        paint.setTextSize(14f);
        paint.setFakeBoldText(false);
        canvas.drawText("Daily Sales Report: " + dateStr, 50, y, paint);

        y += 40;
        // Table Header
        paint.setFakeBoldText(true);
        canvas.drawText("Invoice", 50, y, paint);
        canvas.drawText("Time", 250, y, paint);
        canvas.drawText("Amount", 450, y, paint);
        
        y += 10;
        canvas.drawLine(50, y, 550, y, paint);
        
        y += 25;
        paint.setFakeBoldText(false);
        
        double total = 0;
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        
        for (Sale sale : sales) {
            if (y > 800) { // Simple page overflow handling
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
            
            canvas.drawText(sale.invoiceNumber != null ? sale.invoiceNumber : "ORD" + sale.id, 50, y, paint);
            canvas.drawText(timeFormat.format(new Date(sale.createdAt)), 250, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", sale.totalAmount), 450, y, paint);
            
            total += sale.totalAmount;
            y += 20;
        }

        y += 20;
        paint.setFakeBoldText(true);
        canvas.drawText("Total Sales: Rs. " + String.format(Locale.getDefault(), "%.2f", total), 50, y, paint);

        document.finishPage(page);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
        } finally {
            document.close();
        }

        return file;
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