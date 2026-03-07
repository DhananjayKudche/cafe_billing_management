package com.kudche.cafebillingmanagement.Activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Utils.GoogleDriveHelper;
import com.kudche.cafebillingmanagement.Utils.PdfReportGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReportActivity extends AppCompatActivity {

    private AppDatabase db;
    private TextView tvSelectedDateRange, tvTotalOrders, tvTotalItemsSold, tvTotalSales, tvAvgOrderValue;
    private RecyclerView rvProductSales, rvDayWiseSales, rvDetailedOrders;
    private ProductSalesAdapter productSalesAdapter;
    private DayWiseSalesAdapter dayWiseSalesAdapter;
    private DetailedOrdersAdapter detailedOrdersAdapter;
    private MaterialButton btnManualBackup;

    private long startDate, endDate;
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    
    private List<SaleDao.ProductSalesReport> currentProductReports = new ArrayList<>();
    private List<SaleDao.DayWiseSalesReport> currentDayReports = new ArrayList<>();
    private List<DetailedOrder> currentDetailedOrders = new ArrayList<>();
    private double totalSalesValGlobal = 0;
    private int totalItemsGlobal = 0;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        db = AppDatabase.getInstance(this);

        initViews();
        setupRecyclerViews();
        setupClickListeners();

        // Default to Today
        selectToday();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        tvSelectedDateRange = findViewById(R.id.tvSelectedDateRange);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalItemsSold = findViewById(R.id.tvTotalItemsSold);
        tvTotalSales = findViewById(R.id.tvTotalSales);
        tvAvgOrderValue = findViewById(R.id.tvAvgOrderValue);

        rvProductSales = findViewById(R.id.rvProductSales);
        rvDayWiseSales = findViewById(R.id.rvDayWiseSales);
        rvDetailedOrders = findViewById(R.id.rvDetailedOrders);
        btnManualBackup = findViewById(R.id.btnManualBackup);
    }

    private void setupRecyclerViews() {
        rvProductSales.setLayoutManager(new LinearLayoutManager(this));
        productSalesAdapter = new ProductSalesAdapter();
        rvProductSales.setAdapter(productSalesAdapter);

        rvDayWiseSales.setLayoutManager(new LinearLayoutManager(this));
        dayWiseSalesAdapter = new DayWiseSalesAdapter();
        rvDayWiseSales.setAdapter(dayWiseSalesAdapter);

        rvDetailedOrders.setLayoutManager(new LinearLayoutManager(this));
        detailedOrdersAdapter = new DetailedOrdersAdapter();
        rvDetailedOrders.setAdapter(detailedOrdersAdapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnToday).setOnClickListener(v -> selectToday());
        findViewById(R.id.btnYesterday).setOnClickListener(v -> selectYesterday());
        findViewById(R.id.btnLast7Days).setOnClickListener(v -> selectLastNDays(7));
        findViewById(R.id.btnLast30Days).setOnClickListener(v -> selectLastNDays(30));
        findViewById(R.id.btnCustomRange).setOnClickListener(v -> showCustomRangePicker());

        findViewById(R.id.btnExportCSV).setOnClickListener(v -> checkPermissionAndExport("CSV"));
        findViewById(R.id.btnExportPDF).setOnClickListener(v -> checkPermissionAndExport("PDF"));
        
        btnManualBackup.setOnClickListener(v -> performManualBackup());
    }

    private void performManualBackup() {
        SharedPreferences prefs = getSharedPreferences("CafePrefs", MODE_PRIVATE);
        String account = prefs.getString("googleAccount", null);
        if (account == null) {
            Toast.makeText(this, "Please connect Google Drive in Settings first", Toast.LENGTH_LONG).show();
            return;
        }

        btnManualBackup.setEnabled(false);
        btnManualBackup.setText("Backing up...");

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Generate detailed PDF for the currently selected range
                File reportFile = PdfReportGenerator.generateDailyReport(this, startDate, endDate);

                // 2. Upload to Drive
                GoogleDriveHelper driveHelper = new GoogleDriveHelper(this, account);
                String rootId = driveHelper.getOrCreateFolder("CafeReports", null);
                String yearId = driveHelper.getOrCreateFolder(new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()), rootId);
                String monthId = driveHelper.getOrCreateFolder(new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date()), yearId);
                
                driveHelper.uploadFile(reportFile, "application/pdf", monthId);

                runOnUiThread(() -> {
                    btnManualBackup.setEnabled(true);
                    btnManualBackup.setText("Manual Backup to Drive");
                    Toast.makeText(this, "Backup Successful!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    btnManualBackup.setEnabled(true);
                    btnManualBackup.setText("Manual Backup to Drive");
                    Toast.makeText(this, "Backup Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void checkPermissionAndExport(String type) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        
        if (type.equals("CSV")) exportCSV();
        else exportPDF();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted. Click Export again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied. Cannot save to storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startDate = cal.getTimeInMillis();
        
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        endDate = cal.getTimeInMillis();
        
        updateReport();
    }

    private void selectYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        startDate = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        endDate = cal.getTimeInMillis();

        updateReport();
    }

    private void selectLastNDays(int n) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        endDate = cal.getTimeInMillis();

        cal.add(Calendar.DATE, -(n - 1));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        startDate = cal.getTimeInMillis();

        updateReport();
    }

    private void showCustomRangePicker() {
        DatePickerDialog startPicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            startDate = cal.getTimeInMillis();

            DatePickerDialog endPicker = new DatePickerDialog(this, (view1, year1, month1, dayOfMonth1) -> {
                Calendar calEnd = Calendar.getInstance();
                calEnd.set(year1, month1, dayOfMonth1, 23, 59, 59);
                endDate = calEnd.getTimeInMillis();
                updateReport();
            }, year, month, dayOfMonth);
            endPicker.setTitle("Select End Date");
            endPicker.show();

        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        startPicker.setTitle("Select Start Date");
        startPicker.show();
    }

    private void updateReport() {
        tvSelectedDateRange.setText("Date Range: " + displayFormat.format(new Date(startDate)) + " - " + displayFormat.format(new Date(endDate)));
        
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Sale> sales = db.saleDao().getSalesInRange(startDate, endDate);
            currentProductReports = db.saleDao().getProductWiseSales(startDate, endDate);
            currentDayReports = db.saleDao().getDayWiseSalesFixed(startDate, endDate);
            
            currentDetailedOrders = new ArrayList<>();
            totalItemsGlobal = 0;
            totalSalesValGlobal = 0;

            for (Sale sale : sales) {
                DetailedOrder order = new DetailedOrder();
                order.sale = sale;
                List<SaleItem> items = db.saleDao().getItemsForSale(sale.id);
                order.items = items;
                
                StringBuilder itemsSummary = new StringBuilder();
                for (SaleItem si : items) {
                    Product p = db.productDao().getByIdSync(si.productId);
                    if (p != null) {
                        itemsSummary.append(p.name).append(" x").append(si.quantity).append(", ");
                    }
                    totalItemsGlobal += si.quantity;
                }
                if (itemsSummary.length() > 2) {
                    itemsSummary.setLength(itemsSummary.length() - 2);
                }
                order.itemsSummary = itemsSummary.toString();
                currentDetailedOrders.add(order);
                totalSalesValGlobal += sale.totalAmount;
            }

            final int finalTotalOrders = sales.size();
            final int finalTotalItems = totalItemsGlobal;
            final double finalTotalSales = totalSalesValGlobal;
            final double avgOrder = finalTotalOrders > 0 ? finalTotalSales / finalTotalOrders : 0;

            runOnUiThread(() -> {
                tvTotalOrders.setText(String.valueOf(finalTotalOrders));
                tvTotalItemsSold.setText(String.valueOf(finalTotalItems));
                tvTotalSales.setText("₹" + (int)finalTotalSales);
                tvAvgOrderValue.setText("₹" + (int)avgOrder);

                productSalesAdapter.setReports(currentProductReports);
                dayWiseSalesAdapter.setReports(currentDayReports);
                detailedOrdersAdapter.setOrders(currentDetailedOrders);
                
                findViewById(R.id.tvDayWiseLabel).setVisibility(currentDayReports.size() > 1 ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void exportCSV() {
        if (currentDetailedOrders.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder csv = new StringBuilder();
        csv.append("Sales Report\n");
        csv.append("Date Range: ").append(displayFormat.format(new Date(startDate))).append(" - ").append(displayFormat.format(new Date(endDate))).append("\n\n");
        
        csv.append("Summary\n");
        csv.append("Total Orders,").append(currentDetailedOrders.size()).append("\n");
        csv.append("Total Items Sold,").append(totalItemsGlobal).append("\n");
        csv.append("Total Sales,Rs.").append(totalSalesValGlobal).append("\n\n");

        csv.append("Product Wise Sales\n");
        csv.append("Product,Qty Sold,Unit Price,Total Sales\n");
        for (SaleDao.ProductSalesReport p : currentProductReports) {
            csv.append(p.productName).append(",").append(p.quantitySold).append(",").append(p.unitPrice).append(",").append(p.totalSales).append("\n");
        }
        csv.append("\n");

        csv.append("Day Wise Sales\n");
        csv.append("Date,Orders,Items Sold,Total Sales\n");
        for (SaleDao.DayWiseSalesReport d : currentDayReports) {
            csv.append(d.date).append(",").append(d.orderCount).append(",").append(d.totalItemsSold).append(",").append(d.totalSales).append("\n");
        }
        csv.append("\n");

        csv.append("Detailed Orders\n");
        csv.append("Order ID,Date,Items,Total\n");
        for (DetailedOrder o : currentDetailedOrders) {
            String inv = o.sale.invoiceNumber != null ? o.sale.invoiceNumber : "ORD" + o.sale.id;
            csv.append(inv).append(",").append(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(o.sale.createdAt))).append(",\"").append(o.itemsSummary).append("\",").append(o.sale.totalAmount).append("\n");
        }

        saveFileToDownloads("Sales_Report_" + System.currentTimeMillis() + ".csv", "text/csv", csv.toString().getBytes());
    }

    private void exportPDF() {
        if (currentDetailedOrders.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Use the shared generator for consistency
                File reportFile = PdfReportGenerator.generateDailyReport(this, startDate, endDate);
                
                // Read the file and save it to downloads
                FileInputStream fis = new FileInputStream(reportFile);
                byte[] data = new byte[(int) reportFile.length()];
                fis.read(data);
                fis.close();

                runOnUiThread(() -> {
                    saveFileToDownloads(reportFile.getName(), "application/pdf", data);
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveFileToDownloads(String fileName, String mimeType, byte[] data) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (uri == null) throw new IOException("Failed to create MediaStore entry.");

                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream == null) throw new IOException("Failed to open output stream.");
                    outputStream.write(data);
                    Toast.makeText(this, "Saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                }
            } else {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!path.exists() && !path.mkdirs()) throw new IOException("Failed to create Downloads directory.");
                File file = new File(path, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data);
                    Toast.makeText(this, "Saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static class DetailedOrder {
        Sale sale;
        List<SaleItem> items;
        String itemsSummary;
    }

    private static class ProductSalesAdapter extends RecyclerView.Adapter<ProductSalesAdapter.ViewHolder> {
        private List<SaleDao.ProductSalesReport> reports = new ArrayList<>();
        public void setReports(List<SaleDao.ProductSalesReport> reports) { this.reports = reports; notifyDataSetChanged(); }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_sale, parent, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SaleDao.ProductSalesReport report = reports.get(position);
            holder.name.setText(report.productName);
            holder.qty.setText(String.valueOf(report.quantitySold));
            holder.price.setText("₹" + (int)report.unitPrice);
            holder.total.setText("₹" + (int)report.totalSales);
        }
        @Override public int getItemCount() { return reports.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, qty, price, total;
            public ViewHolder(@NonNull View v) {
                super(v);
                name = v.findViewById(R.id.tvProductName);
                qty = v.findViewById(R.id.tvQuantity);
                price = v.findViewById(R.id.tvUnitPrice);
                total = v.findViewById(R.id.tvTotalSales);
            }
        }
    }

    private static class DayWiseSalesAdapter extends RecyclerView.Adapter<DayWiseSalesAdapter.ViewHolder> {
        private List<SaleDao.DayWiseSalesReport> reports = new ArrayList<>();
        public void setReports(List<SaleDao.DayWiseSalesReport> reports) { this.reports = reports; notifyDataSetChanged(); }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_wise_sale, parent, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SaleDao.DayWiseSalesReport report = reports.get(position);
            holder.date.setText(report.date);
            holder.orders.setText(String.valueOf(report.orderCount));
            holder.items.setText(String.valueOf(report.totalItemsSold));
            holder.total.setText("₹" + (int)report.totalSales);
        }
        @Override public int getItemCount() { return reports.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView date, orders, items, total;
            public ViewHolder(@NonNull View v) {
                super(v);
                date = v.findViewById(R.id.tvDate);
                orders = v.findViewById(R.id.tvOrderCount);
                items = v.findViewById(R.id.tvItemsSold);
                total = v.findViewById(R.id.tvTotalSales);
            }
        }
    }

    private static class DetailedOrdersAdapter extends RecyclerView.Adapter<DetailedOrdersAdapter.ViewHolder> {
        private List<DetailedOrder> orders = new ArrayList<>();
        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        public void setOrders(List<DetailedOrder> orders) { this.orders = orders; notifyDataSetChanged(); }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detailed_order, parent, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DetailedOrder order = orders.get(position);
            holder.id.setText(order.sale.invoiceNumber != null ? order.sale.invoiceNumber : "ORD" + order.sale.id);
            holder.date.setText(dateFormat.format(new Date(order.sale.createdAt)));
            holder.items.setText(order.itemsSummary);
            holder.total.setText("₹" + (int)order.sale.totalAmount);
        }
        @Override public int getItemCount() { return orders.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView id, date, items, total;
            public ViewHolder(@NonNull View v) {
                super(v);
                id = v.findViewById(R.id.tvOrderId);
                date = v.findViewById(R.id.tvDate);
                items = v.findViewById(R.id.tvItems);
                total = v.findViewById(R.id.tvTotal);
            }
        }
    }
}