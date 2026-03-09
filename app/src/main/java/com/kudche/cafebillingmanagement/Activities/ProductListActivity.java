package com.kudche.cafebillingmanagement.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.kudche.cafebillingmanagement.Adapters.ProductAdapter;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Utils.ToastUtils;
import com.kudche.cafebillingmanagement.ViewModel.ProductViewModel;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnEditClick, ProductAdapter.OnDeleteClick {

    private ProductViewModel viewModel;
    private ProductAdapter adapter;
    private String currentCategory = "Cafe Category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> showLogoutDialog());

        RecyclerView recyclerView = findViewById(R.id.productRecycler);
        ExtendedFloatingActionButton addFab = findViewById(R.id.addProductFab);

        adapter = new ProductAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        
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

        addFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    private void observeProducts() {
        viewModel.getProductsByCategory(currentCategory).removeObservers(this);
        viewModel.getProductsByCategory(currentCategory).observe(this, products -> {
            if (products != null) {
                adapter.setList(products);
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    getSharedPreferences("CafePrefs", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEdit(Product product) {
        Intent intent = new Intent(this, AddProductActivity.class);
        intent.putExtra("productId", product.id);
        startActivity(intent);
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.name + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.delete(product);
                    ToastUtils.showInfo(this, "Product deleted");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}