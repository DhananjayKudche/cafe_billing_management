package com.kudche.cafebillingmanagement.BackupManager;

import android.content.Context;
import com.google.gson.Gson;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.Utils.GoogleDriveHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DriveBackupManager {

    private final Context context;
    private final GoogleDriveHelper driveHelper;
    private static final String BACKUP_FILE_NAME = "products_backup.json";
    private static final String BACKUP_FOLDER_NAME = "Cafe_Data_Backup";

    public DriveBackupManager(Context context, String accountName) {
        this.context = context;
        this.driveHelper = new GoogleDriveHelper(context, accountName);
    }

    /**
     * Uploads the daily report PDF to Drive with the specified folder structure:
     * Cafe_Bills -> Year -> Month -> [Daily_Report_DD_MMM_YYYY.pdf]
     */
    public void uploadDailyReport(File reportFile, long timestamp) throws IOException {
        Date date = new Date(timestamp);
        String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(date);
        String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(date);
        String dayFormatted = new SimpleDateFormat("dd_MMMM_yyyy", Locale.getDefault()).format(date);

        File renamedFile = new File(context.getCacheDir(), "Daily_Report_" + dayFormatted + ".pdf");
        if (reportFile.renameTo(renamedFile)) {
            reportFile = renamedFile;
        }

        String rootId = driveHelper.getOrCreateFolder("Cafe_Bills", null);
        String yearId = driveHelper.getOrCreateFolder(year, rootId);
        String monthId = driveHelper.getOrCreateFolder(month, yearId);

        driveHelper.uploadFile(reportFile, "application/pdf", monthId);
    }

    /**
     * Creates a JSON backup of Products and Raw Materials and uploads it to Drive.
     */
    public void uploadDataBackup() throws IOException {
        AppDatabase db = AppDatabase.getInstance(context);
        
        DataBackup backup = new DataBackup();
        backup.products = db.productDao().getAllSync();
        backup.rawMaterials = db.rawMaterialDao().getAllSync();
        backup.productRawMaterials = db.productRawMaterialDao().getAllSync();

        String json = new Gson().toJson(backup);
        File backupFile = new File(context.getCacheDir(), BACKUP_FILE_NAME);
        
        try (FileOutputStream fos = new FileOutputStream(backupFile)) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
        }

        String folderId = driveHelper.getOrCreateFolder(BACKUP_FOLDER_NAME, null);
        driveHelper.uploadFile(backupFile, "application/json", folderId);
    }

    /**
     * Downloads the JSON backup from Drive and restores it to the local database.
     */
    public void restoreDataBackup() throws IOException {
        String folderId = driveHelper.getOrCreateFolder(BACKUP_FOLDER_NAME, null);
        String json = driveHelper.downloadFileContent(BACKUP_FILE_NAME, folderId);

        if (json == null || json.isEmpty()) {
            throw new IOException("Backup file not found on Google Drive");
        }

        DataBackup backup = new Gson().fromJson(json, DataBackup.class);
        if (backup == null) return;

        AppDatabase db = AppDatabase.getInstance(context);
        db.runInTransaction(() -> {
            // Clear existing data
            db.productDao().deleteAll();
            // Note: Room might need more specific delete queries for other tables if CASCADE isn't enough
            // Since version 16 seeds data, we might want to clear those too.
            
            // Restore Raw Materials
            if (backup.rawMaterials != null) {
                for (RawMaterial rm : backup.rawMaterials) {
                    db.rawMaterialDao().insert(rm);
                }
            }

            // Restore Products
            if (backup.products != null) {
                for (Product p : backup.products) {
                    db.productDao().insert(p);
                }
            }

            // Restore Mappings
            if (backup.productRawMaterials != null) {
                for (ProductRawMaterial prm : backup.productRawMaterials) {
                    db.productRawMaterialDao().insert(prm);
                }
            }
        });
    }

    public static class DataBackup {
        public List<Product> products;
        public List<RawMaterial> rawMaterials;
        public List<ProductRawMaterial> productRawMaterials;
    }
}