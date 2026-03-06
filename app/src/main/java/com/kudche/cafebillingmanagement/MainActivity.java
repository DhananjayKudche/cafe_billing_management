package com.kudche.cafebillingmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.kudche.cafebillingmanagement.Activities.BillingActivity;
import com.kudche.cafebillingmanagement.Activities.DayCloseActivity;
import com.kudche.cafebillingmanagement.Activities.LoginActivity;
import com.kudche.cafebillingmanagement.Activities.ProductListActivity;
import com.kudche.cafebillingmanagement.Activities.RawMaterialActivity;
import com.kudche.cafebillingmanagement.Activities.ReportActivity;
import com.kudche.cafebillingmanagement.Activities.SaleHistoryActivity;

public class MainActivity extends AppCompatActivity {

    CardView billingCard, historyCard, productCard, inventoryCard, dayCloseCard, reportsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("CafePrefs", MODE_PRIVATE);
        String role = prefs.getString("userRole", "WORKER");

        billingCard = findViewById(R.id.cardBilling);
        historyCard = findViewById(R.id.cardHistory);
        productCard = findViewById(R.id.cardProduct);
        inventoryCard = findViewById(R.id.cardRawMaterial);
        dayCloseCard = findViewById(R.id.cardDayClose);
        reportsCard = findViewById(R.id.cardReports);
        
        // Role-based visibility
        if ("WORKER".equals(role)) {
            if (reportsCard != null) reportsCard.setVisibility(View.GONE);
            if (productCard != null) productCard.setVisibility(View.GONE);
            if (inventoryCard != null) inventoryCard.setVisibility(View.GONE);
            // Worker can do Billing, Sale History, and Day Close
        }

        billingCard.setOnClickListener(v ->
                startActivity(new Intent(this, BillingActivity.class)));

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

        if (reportsCard != null && reportsCard.getVisibility() == View.VISIBLE) {
            reportsCard.setOnClickListener(v ->
                    startActivity(new Intent(this, ReportActivity.class)));
        }
        
        // Logout functionality (Long press on background)
        findViewById(R.id.main_layout).setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Do you want to logout?")
                    .setPositiveButton("Logout", (d, w) -> {
                        prefs.edit().clear().apply();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }
}