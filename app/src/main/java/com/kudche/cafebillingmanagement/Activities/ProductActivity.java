package com.kudche.cafebillingmanagement.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kudche.cafebillingmanagement.Adapters.ProductAdapter;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.ViewModel.ProductViewModel;

public class ProductActivity extends AppCompatActivity {

    ProductViewModel viewModel;
    ProductAdapter adapter;
    private String currentCategory = "Cafe Category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        RecyclerView recycler = findViewById(R.id.productRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new ProductAdapter(
                product -> { // EDIT
                    Intent intent = new Intent(this, AddProductActivity.class);
                    intent.putExtra("productId", product.id);
                    startActivity(intent);
                },
                product -> { // DELETE
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Product")
                            .setMessage("Delete " + product.name + "?")
                            .setPositiveButton("Yes", (d,w)-> viewModel.delete(product))
                            .setNegativeButton("No",null)
                            .show();
                }
        );

        recycler.setAdapter(adapter);

        // Category Chips
        ChipGroup categoryChipGroup = findViewById(R.id.categoryChipGroup);
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipCafe)) {
                currentCategory = "Cafe Category";
            } else if (checkedIds.contains(R.id.chipJuice)) {
                currentCategory = "Juice Category";
            }
            observeProducts();
        });

        observeProducts();

        FloatingActionButton fab = findViewById(R.id.fabAddProduct);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
    }

    private void observeProducts() {
        // Remove previous observers to avoid multiple calls
        viewModel.getProductsByCategory(currentCategory).removeObservers(this);
        viewModel.getProductsByCategory(currentCategory).observe(this, products -> {
            adapter.setList(products);
        });
    }
}