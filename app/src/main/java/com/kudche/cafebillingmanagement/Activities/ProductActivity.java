package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.ViewModel.ProductViewModel;

public class ProductActivity extends AppCompatActivity {

    private ProductViewModel viewModel;

    EditText productName;
    EditText productPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        Button addProduct = findViewById(R.id.addProductBtn);

        addProduct.setOnClickListener(v -> {

            String name = productName.getText().toString().trim();
            String priceText = productPrice.getText().toString().trim();

            if(name.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(this, "Enter product name and price", Toast.LENGTH_SHORT).show();
                return;
            }

            Product product = new Product();

            product.name = name;
            product.price = Double.parseDouble(priceText);
            product.currentStock = 50;
            product.lowStockThreshold = 5;

            viewModel.addProduct(product);

            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();

            productName.setText("");
            productPrice.setText("");

        });
    }

}