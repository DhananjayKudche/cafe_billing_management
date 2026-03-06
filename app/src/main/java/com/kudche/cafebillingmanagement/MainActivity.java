package com.kudche.cafebillingmanagement;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.kudche.cafebillingmanagement.Activities.BillingActivity;
import com.kudche.cafebillingmanagement.Activities.DayCloseActivity;
import com.kudche.cafebillingmanagement.Activities.ProductListActivity;
import com.kudche.cafebillingmanagement.Activities.RawMaterialActivity;
import com.kudche.cafebillingmanagement.Activities.SaleHistoryActivity;
import com.kudche.cafebillingmanagement.R;

public class MainActivity extends AppCompatActivity {

    CardView billingCard, historyCard, productCard, inventoryCard, dayCloseCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        billingCard = findViewById(R.id.cardBilling);
        historyCard = findViewById(R.id.cardHistory);
        productCard = findViewById(R.id.cardProduct);
        inventoryCard = findViewById(R.id.cardRawMaterial);
        dayCloseCard = findViewById(R.id.cardDayClose);

        billingCard.setOnClickListener(v ->
                startActivity(new Intent(this, BillingActivity.class)));

        historyCard.setOnClickListener(v ->
                startActivity(new Intent(this, SaleHistoryActivity.class)));

        productCard.setOnClickListener(v ->
                startActivity(new Intent(this, ProductListActivity.class)));

        inventoryCard.setOnClickListener(v ->
                startActivity(new Intent(this, RawMaterialActivity.class)));

        dayCloseCard.setOnClickListener(v ->
                startActivity(new Intent(this, DayCloseActivity.class)));
    }
}