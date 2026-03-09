package com.kudche.cafebillingmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.kudche.cafebillingmanagement.Activities.BillingActivity;
import com.kudche.cafebillingmanagement.Activities.DayCloseActivity;
import com.kudche.cafebillingmanagement.Activities.LoginActivity;
import com.kudche.cafebillingmanagement.Activities.ProductListActivity;
import com.kudche.cafebillingmanagement.Activities.RawMaterialActivity;
import com.kudche.cafebillingmanagement.Activities.ReportActivity;
import com.kudche.cafebillingmanagement.Activities.SaleHistoryActivity;
import com.kudche.cafebillingmanagement.Activities.SettingsActivity;
import com.kudche.cafebillingmanagement.Utils.PermissionHelper;

public class MainActivity extends AppCompatActivity {

    CardView billingCard, historyCard, productCard, inventoryCard, dayCloseCard, reportsCard;
    ImageView ivSettings;
    ImageButton ivLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("CafePrefs", MODE_PRIVATE);
        String role = prefs.getString("userRole", "WORKER");

        // Redirect Worker to Billing immediately
        if ("WORKER".equals(role)) {
            startActivity(new Intent(this, BillingActivity.class));
            finish();
            return;
        }

        billingCard = findViewById(R.id.cardBilling);
        historyCard = findViewById(R.id.cardHistory);
        productCard = findViewById(R.id.cardProduct);
        inventoryCard = findViewById(R.id.cardRawMaterial);
        dayCloseCard = findViewById(R.id.cardDayClose);
        reportsCard = findViewById(R.id.cardReports);
        ivSettings = findViewById(R.id.ivSettings);
        ivLogout = findViewById(R.id.ivLogout);
        
        if (ivLogout != null) {
            ivLogout.setOnClickListener(v -> showLogoutDialog(prefs));
        }

        if (billingCard != null) {
            billingCard.setOnClickListener(v ->
                    startActivity(new Intent(this, BillingActivity.class)));
        }

        historyCard.setOnClickListener(v ->
                startActivity(new Intent(this, SaleHistoryActivity.class)));

        if (productCard != null) {
            productCard.setOnClickListener(v ->
                    startActivity(new Intent(this, ProductListActivity.class)));
        }

        if (inventoryCard != null) {
            inventoryCard.setOnClickListener(v ->
                    startActivity(new Intent(this, RawMaterialActivity.class)));
        }

        dayCloseCard.setOnClickListener(v ->
                startActivity(new Intent(this, DayCloseActivity.class)));

        if (reportsCard != null) {
            reportsCard.setOnClickListener(v ->
                    startActivity(new Intent(this, ReportActivity.class)));
        }

        if (ivSettings != null) {
            ivSettings.setOnClickListener(v ->
                    startActivity(new Intent(this, SettingsActivity.class)));
        }
        
        // Request Printer Permissions on Start
        if (!PermissionHelper.hasPrintPermissions(this)) {
            PermissionHelper.requestPrintPermissions(this);
        }
    }

    private void showLogoutDialog(SharedPreferences prefs) {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.REQ_CODE_PRINT_PERMISSIONS) {
            if (PermissionHelper.hasPrintPermissions(this)) {
                Toast.makeText(this, "Printer permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Printer permissions are required for billing", Toast.LENGTH_LONG).show();
            }
        }
    }
}