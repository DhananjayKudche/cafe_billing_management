package com.kudche.cafebillingmanagement;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.kudche.cafebillingmanagement.Activities.BillingActivity;
import com.kudche.cafebillingmanagement.Activities.ProductActivity;
import com.kudche.cafebillingmanagement.Activities.RawMaterialActivity;
import com.kudche.cafebillingmanagement.Activities.RecipeActivity;
import com.kudche.cafebillingmanagement.R;

public class MainActivity extends AppCompatActivity {

    CardView billingCard, productCard, rawMaterialCard, recipeCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        billingCard = findViewById(R.id.cardBilling);
        productCard = findViewById(R.id.cardProduct);
        rawMaterialCard = findViewById(R.id.cardRawMaterial);
        recipeCard = findViewById(R.id.cardRecipe);

        billingCard.setOnClickListener(v ->
                startActivity(new Intent(this, BillingActivity.class)));

        productCard.setOnClickListener(v ->
                startActivity(new Intent(this, ProductActivity.class)));

        rawMaterialCard.setOnClickListener(v ->
                startActivity(new Intent(this, RawMaterialActivity.class)));

        recipeCard.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeActivity.class)));
    }
}