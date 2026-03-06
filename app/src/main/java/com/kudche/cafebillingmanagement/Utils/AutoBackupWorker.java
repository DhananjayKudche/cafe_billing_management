package com.kudche.cafebillingmanagement.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AutoBackupWorker extends Worker {

    public AutoBackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("CafePrefs", Context.MODE_PRIVATE);
        
        String account = prefs.getString("googleAccount", null);
        boolean autoBackupEnabled = prefs.getBoolean("autoBackup", false);

        if (account == null || !autoBackupEnabled) {
            return Result.success();
        }

        try {
            // 1. Generate PDF for yesterday (usually daily backup runs at midnight or end of day)
            // For simplicity, we'll backup whatever the current day's report is at the time of execution
            File reportFile = PdfReportGenerator.generateDailyReport(context, System.currentTimeMillis());

            // 2. Upload to Drive
            GoogleDriveHelper driveHelper = new GoogleDriveHelper(context, account);
            
            String rootId = driveHelper.getOrCreateFolder("CafeReports", null);
            String yearId = driveHelper.getOrCreateFolder(new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()), rootId);
            String monthId = driveHelper.getOrCreateFolder(new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date()), yearId);
            
            driveHelper.uploadFile(reportFile, "application/pdf", monthId);

            String now = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date());
            prefs.edit().putString("lastBackup", now).apply();

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}