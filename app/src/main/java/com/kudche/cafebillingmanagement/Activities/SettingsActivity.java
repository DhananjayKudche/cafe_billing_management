package com.kudche.cafebillingmanagement.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Utils.AutoBackupWorker;
import com.kudche.cafebillingmanagement.Utils.GoogleDriveHelper;
import com.kudche.cafebillingmanagement.Utils.PdfReportGenerator;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvAccountName, tvLastBackup;
    private Button btnConnectDrive, btnBackupNow;
    private SwitchMaterial switchAutoBackup;
    private SharedPreferences prefs;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
        switchAutoBackup = findViewById(R.id.switchAutoBackup);

        updateUI();

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .addOnSuccessListener(this::handleSignInSuccess)
                                .addOnFailureListener(e -> Toast.makeText(this, "Sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
        );

        btnConnectDrive.setOnClickListener(v -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                    .build();
            GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
            googleSignInLauncher.launch(client.getSignInIntent());
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
        
        Toast.makeText(this, "Auto Backup Scheduled", Toast.LENGTH_SHORT).show();
    }

    private void cancelAutoBackup() {
        WorkManager.getInstance(this).cancelUniqueWork("DailyBackup");
        Toast.makeText(this, "Auto Backup Disabled", Toast.LENGTH_SHORT).show();
    }

    private void handleSignInSuccess(GoogleSignInAccount account) {
        prefs.edit().putString("googleAccount", account.getEmail()).apply();
        updateUI();
        Toast.makeText(this, "Connected: " + account.getEmail(), Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        String account = prefs.getString("googleAccount", null);
        if (account != null) {
            tvAccountName.setText("Connected: " + account);
            btnConnectDrive.setText("Switch Account");
        } else {
            tvAccountName.setText("Not Connected");
            btnConnectDrive.setText("Connect Google Drive");
        }

        switchAutoBackup.setChecked(prefs.getBoolean("autoBackup", false));
        tvLastBackup.setText("Last Backup: " + prefs.getString("lastBackup", "Never"));
    }

    private void performBackup() {
        String account = prefs.getString("googleAccount", null);
        if (account == null) {
            Toast.makeText(this, "Please connect Google Drive first", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    btnBackupNow.setEnabled(false);
                    btnBackupNow.setText("Syncing Data...");
                });
                
                // 1. Generate PDF
                File reportFile = PdfReportGenerator.generateDailyReport(this, System.currentTimeMillis());

                // 2. Upload to Drive
                GoogleDriveHelper driveHelper = new GoogleDriveHelper(this, account);
                
                // Folder structure: CafeReports / Year / Month
                String rootId = driveHelper.getOrCreateFolder("CafeReports", null);
                String yearId = driveHelper.getOrCreateFolder(new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()), rootId);
                String monthId = driveHelper.getOrCreateFolder(new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date()), yearId);
                
                driveHelper.uploadFile(reportFile, "application/pdf", monthId);

                String now = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date());
                prefs.edit().putString("lastBackup", now).apply();

                runOnUiThread(() -> {
                    updateUI();
                    btnBackupNow.setEnabled(true);
                    btnBackupNow.setText("Sync Data Now");
                    Toast.makeText(this, "Backup Successful!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    btnBackupNow.setEnabled(true);
                    btnBackupNow.setText("Sync Data Now");
                    Toast.makeText(this, "Backup Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}