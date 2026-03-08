package com.kudche.cafebillingmanagement.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kudche.cafebillingmanagement.BackupManager.DriveBackupManager;

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
            DriveBackupManager backupManager = new DriveBackupManager(context, account);
            
            // 1. Backup Bills (Daily Report)
            // Generate PDF for the current state (as a daily snapshot)
            long now = System.currentTimeMillis();
            File reportFile = PdfReportGenerator.generateDailyReport(context, now);
            backupManager.uploadDailyReport(reportFile, now);

            // 2. Backup Data (Products & Raw Materials)
            backupManager.uploadDataBackup();

            String timeStr = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date());
            prefs.edit().putString("lastBackup", timeStr).apply();

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}