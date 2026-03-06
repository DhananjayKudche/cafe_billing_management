package com.kudche.cafebillingmanagement.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.kudche.cafebillingmanagement.R;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Navigation to Billing
        findViewById(R.id.cardBilling).setOnClickListener(v -> {
            startActivity(new Intent(this, BillingActivity.class));
        });

        // Navigation to Products
        findViewById(R.id.cardProduct).setOnClickListener(v -> {
            startActivity(new Intent(this, ProductListActivity.class));
        });

        // Navigation to Sale History
        findViewById(R.id.cardHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, SaleHistoryActivity.class));
        });
    }
}