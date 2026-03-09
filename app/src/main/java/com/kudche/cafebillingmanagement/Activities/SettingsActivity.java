package com.kudche.cafebillingmanagement.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kudche.cafebillingmanagement.BackupManager.DriveBackupManager;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Utils.AutoBackupWorker;
import com.kudche.cafebillingmanagement.Utils.PdfReportGenerator;
import com.kudche.cafebillingmanagement.Utils.ToastUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private TextView tvAccountName, tvLastBackup;
    private Button btnConnectDrive, btnBackupNow, btnRestoreData;
    private SwitchMaterial switchAutoBackup;
    private SharedPreferences prefs;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        prefs = getSharedPreferences("CafePrefs", MODE_PRIVATE);

        tvAccountName = findViewById(R.id.tvAccountName);
        tvLastBackup = findViewById(R.id.tvLastBackup);
        btnConnectDrive = findViewById(R.id.btnConnectDrive);
        btnBackupNow = findViewById(R.id.btnBackupNow);
        btnRestoreData = findViewById(R.id.btnRestoreData);
        switchAutoBackup = findViewById(R.id.switchAutoBackup);

        updateUI();

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        handleSignInSuccess(account);
                    } catch (ApiException e) {
                        int statusCode = e.getStatusCode();
                        Log.e(TAG, "Sign-in failed. Status Code: " + statusCode, e);
                        
                        String message;
                        switch (statusCode) {
                            case 7: message = "Network Error. Check your internet."; break;
                            case 10: message = "Developer Error (10): Likely SHA-1 mismatch."; break;
                            case 12500: message = "Sign-in Failed (12500)."; break;
                            case 12501: message = "Sign-in Cancelled."; break;
                            default: message = "Sign-in error: " + statusCode; break;
                        }
                        ToastUtils.showError(this, message);
                    }
                }
        );

        btnConnectDrive.setOnClickListener(v -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                    .build();
            GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
            
            client.signOut().addOnCompleteListener(task -> {
                googleSignInLauncher.launch(client.getSignInIntent());
            });
        });

        switchAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("autoBackup", isChecked).apply();
            if (isChecked) {
                scheduleAutoBackup();
            } else {
                cancelAutoBackup();
            }
        });

        btnBackupNow.setOnClickListener(v -> performBackup());
        
        btnRestoreData.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Restore Data")
                    .setMessage("This will replace current data with Google Drive backup. Continue?")
                    .setPositiveButton("Restore", (d, w) -> performRestore())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scheduleAutoBackup() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest backupRequest = new PeriodicWorkRequest.Builder(
                AutoBackupWorker.class, 24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyBackup",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                backupRequest);
        
        ToastUtils.showSuccess(this, "Auto Backup Scheduled");
    }

    private void cancelAutoBackup() {
        WorkManager.getInstance(this).cancelUniqueWork("DailyBackup");
        ToastUtils.showInfo(this, "Auto Backup Disabled");
    }

    private void handleSignInSuccess(GoogleSignInAccount account) {
        if (account.getEmail() != null) {
            prefs.edit().putString("googleAccount", account.getEmail()).apply();
            updateUI();
            ToastUtils.showSuccess(this, "Connected: " + account.getEmail());
        }
    }

    private void updateUI() {
        String account = prefs.getString("googleAccount", null);
        if (account != null) {
            tvAccountName.setText("Connected: " + account);
            btnConnectDrive.setText("Switch Account");
            btnRestoreData.setEnabled(true);
        } else {
            tvAccountName.setText("Not Connected");
            btnConnectDrive.setText("Connect Google Drive");
            btnRestoreData.setEnabled(false);
        }

        switchAutoBackup.setChecked(prefs.getBoolean("autoBackup", false));
        tvLastBackup.setText("Last Backup: " + prefs.getString("lastBackup", "Never"));
    }

    private void performBackup() {
        String account = prefs.getString("googleAccount", null);
        if (account == null) {
            ToastUtils.showInfo(this, "Please connect Google Drive first");
            return;
        }

        new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    btnBackupNow.setEnabled(false);
                    btnBackupNow.setText("Syncing...");
                });
                
                DriveBackupManager backupManager = new DriveBackupManager(this, account);
                long now = System.currentTimeMillis();
                File reportFile = PdfReportGenerator.generateDailyReport(this, now);
                backupManager.uploadDailyReport(reportFile, now);
                backupManager.uploadDataBackup();

                String timeStr = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(new Date());
                prefs.edit().putString("lastBackup", timeStr).apply();

                runOnUiThread(() -> {
                    updateUI();
                    btnBackupNow.setEnabled(true);
                    btnBackupNow.setText("Sync Data Now");
                    ToastUtils.showSuccess(this, "Backup Successful!");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnBackupNow.setEnabled(true);
                    btnBackupNow.setText("Sync Data Now");
                    ToastUtils.showError(this, "Backup Failed");
                });
            }
        }).start();
    }

    private void performRestore() {
        String account = prefs.getString("googleAccount", null);
        if (account == null) return;

        new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    btnRestoreData.setEnabled(false);
                    btnRestoreData.setText("Restoring...");
                });

                DriveBackupManager backupManager = new DriveBackupManager(this, account);
                backupManager.restoreDataBackup();

                runOnUiThread(() -> {
                    btnRestoreData.setEnabled(true);
                    btnRestoreData.setText("Restore Master Data");
                    ToastUtils.showSuccess(this, "Restore Successful!");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnRestoreData.setEnabled(true);
                    btnRestoreData.setText("Restore Master Data");
                    ToastUtils.showError(this, "Restore Failed");
                });
            }
        }).start();
    }
}