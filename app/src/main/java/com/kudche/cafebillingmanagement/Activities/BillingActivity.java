package com.kudche.cafebillingmanagement.Activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.kudche.cafebillingmanagement.Adapters.Sale.BillingProductAdapter;
import com.kudche.cafebillingmanagement.Adapters.Sale.CartAdapter;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.CartItem;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Repository.SaleRepository;
import com.kudche.cafebillingmanagement.Utils.PrinterUtils;
import com.kudche.cafebillingmanagement.Utils.StockManager;
import com.kudche.cafebillingmanagement.ViewModel.ProductViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingActivity extends AppCompatActivity
        implements BillingProductAdapter.ProductClickListener,
        CartAdapter.CartActionListener {

    private List<CartItem> cartItems = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    private int productPage = 0;
    private final int PORTRAIT_PAGE_SIZE = 6;
    private final int LANDSCAPE_PAGE_SIZE = 10;

    private SaleRepository saleRepository;
    private CartAdapter cartAdapter;
    private BillingProductAdapter productAdapter;

    private TextView totalAmount, billingTitle;
    private ProductViewModel productViewModel;
    private AppDatabase db;

    private Button prevProductBtn, nextProductBtn;
    private Button continueBtn, cancelSelectionBtn, cancelCheckoutBtn;
    private CheckBox cbParcel;
    private ImageButton backBtn;
    private LinearLayout layoutProductSelection, layoutCheckout;
    private RecyclerView productRecycler;

    private boolean isCheckoutPage = false;
    private String userRole;
    private String currentCategory = "Cafe Category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        db = AppDatabase.getInstance(this);
        saleRepository = new SaleRepository(this);

        SharedPreferences prefs = getSharedPreferences("CafePrefs", MODE_PRIVATE);
        userRole = prefs.getString("userRole", "WORKER");

        // Views
        billingTitle = findViewById(R.id.billingTitle);
        layoutProductSelection = findViewById(R.id.layoutProductSelection);
        layoutCheckout = findViewById(R.id.layoutCheckout);
        
        productRecycler = findViewById(R.id.productRecycler);
        RecyclerView cartRecycler = findViewById(R.id.cartRecycler);
        totalAmount = findViewById(R.id.totalAmount);

        Button completeSaleBtn = findViewById(R.id.completeSale);
        prevProductBtn = findViewById(R.id.prevProductBtn);
        nextProductBtn = findViewById(R.id.nextProductBtn);
        backBtn = findViewById(R.id.backBtn);
        
        continueBtn = findViewById(R.id.continueBtn);
        cancelSelectionBtn = findViewById(R.id.cancelSelectionBtn);
        cancelCheckoutBtn = findViewById(R.id.cancelCheckoutBtn);
        cbParcel = findViewById(R.id.cbParcel);

        // Visibility Logic for Back Button
        if ("WORKER".equals(userRole)) {
            backBtn.setVisibility(View.GONE);
            cancelSelectionBtn.setText("Logout");
            cancelSelectionBtn.setOnClickListener(v -> showLogoutDialog());
        } else {
            backBtn.setVisibility(View.VISIBLE);
            backBtn.setOnClickListener(v -> handleBackAction());
            cancelSelectionBtn.setOnClickListener(v -> finish());
        }

        cancelCheckoutBtn.setOnClickListener(v -> showProductSelection());
        
        continueBtn.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
            } else {
                showCheckout();
            }
        });

        productAdapter = new BillingProductAdapter(this);
        updateGridLayout();
        productRecycler.setAdapter(productAdapter);

        cartAdapter = new CartAdapter(this);
        cartRecycler.setLayoutManager(new LinearLayoutManager(this));
        cartRecycler.setAdapter(cartAdapter);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        
        // Category Chips
        ChipGroup categoryChipGroup = findViewById(R.id.categoryChipGroup);
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipCafe)) {
                currentCategory = "Cafe Category";
            } else if (checkedIds.contains(R.id.chipJuice)) {
                currentCategory = "Juice Category";
            }
            productPage = 0;
            observeProducts();
        });

        observeProducts();

        prevProductBtn.setOnClickListener(v -> { if (productPage > 0) { productPage--; updateProductList(); } });
        nextProductBtn.setOnClickListener(v -> { if ((productPage + 1) * getPageSize() < filteredProducts.size()) { productPage++; updateProductList(); } });

        completeSaleBtn.setOnClickListener(v -> checkStockAndProceed());
        
        // Handle Back Press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackAction();
            }
        });

        showProductSelection();
    }

    private void observeProducts() {
        productViewModel.getProductsByCategory(currentCategory).removeObservers(this);
        productViewModel.getProductsByCategory(currentCategory).observe(this, products -> {
            filteredProducts = products;
            updateProductList();
        });
    }

    private void updateGridLayout() {
        int spanCount = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ? 5 : 3;
        productRecycler.setLayoutManager(new GridLayoutManager(this, spanCount));
    }

    private int getPageSize() {
        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ? LANDSCAPE_PAGE_SIZE : PORTRAIT_PAGE_SIZE;
    }

    private void handleBackAction() {
        if (isCheckoutPage) {
            showProductSelection();
        } else {
            if ("WORKER".equals(userRole)) {
                // Do nothing for worker on main screen
            } else {
                finish();
            }
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    getSharedPreferences("CafePrefs", MODE_PRIVATE).edit().clear().apply();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProductSelection() {
        isCheckoutPage = false;
        billingTitle.setText("Select Products");
        layoutProductSelection.setVisibility(View.VISIBLE);
        layoutCheckout.setVisibility(View.GONE);
    }

    private void showCheckout() {
        isCheckoutPage = true;
        billingTitle.setText("Review Order");
        layoutProductSelection.setVisibility(View.GONE);
        layoutCheckout.setVisibility(View.VISIBLE);
        updateCart();
    }

    private void checkStockAndProceed() {
        if(cartItems.isEmpty()){
            Toast.makeText(this, "Cart Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            List<String> lowStockItems = new ArrayList<>();
            boolean hasLowStock = false;

            for(CartItem cart : cartItems){
                boolean stockOk = StockManager.isStockAvailable(db, cart.product.id, cart.quantity);
                if(!stockOk){
                    hasLowStock = true;
                    lowStockItems.add(cart.product.name);
                }
            }

            if(hasLowStock) {
                String itemsStr = String.join(", ", lowStockItems);
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Stock Warning")
                            .setMessage("Insufficient stock for: " + itemsStr + "\nProceed with billing anyway?")
                            .setPositiveButton("Proceed (Emergency)", (d, w) -> finalizeSale(true))
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            } else {
                finalizeSale(false);
            }
        }).start();
    }

    private void finalizeSale(boolean isEmergency) {
        List<SaleItem> saleItems = new ArrayList<>();
        for(CartItem cart : cartItems){
            SaleItem item = new SaleItem();
            item.productId = cart.product.id;
            item.quantity = cart.quantity;
            saleItems.add(item);
        }

        boolean isParcel = cbParcel.isChecked();

        saleRepository.createSale(saleItems, isEmergency, isParcel, new SaleRepository.SaleCallback() {
            @Override
            public void onSuccess(Sale sale, List<SaleItem> items) {
                runOnUiThread(() -> {
                    Toast.makeText(BillingActivity.this, isEmergency ? "Emergency Sale Recorded" : "Sale Completed", Toast.LENGTH_SHORT).show();
                    
                    // Trigger Print
                    PrinterUtils.printReceipt(BillingActivity.this, sale, items);
                    
                    cartItems.clear();
                    cbParcel.setChecked(false);
                    updateCart();
                    updateProductQuantities();
                    showProductSelection(); 
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(BillingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateProductList() {
        int pageSize = getPageSize();
        int start = productPage * pageSize;
        int end = Math.min(start + pageSize, filteredProducts.size());
        if (start < filteredProducts.size()) productAdapter.setProducts(filteredProducts.subList(start, end));
        else productAdapter.setProducts(new ArrayList<>());
        prevProductBtn.setVisibility(filteredProducts.size() > pageSize ? View.VISIBLE : View.GONE);
        nextProductBtn.setVisibility(filteredProducts.size() > pageSize ? View.VISIBLE : View.GONE);
        updateProductQuantities();
    }

    private void updateProductQuantities() {
        Map<Integer, Integer> quantities = new HashMap<>();
        for (CartItem item : cartItems) quantities.put(item.product.id, item.quantity);
        productAdapter.setQuantities(quantities);
    }

    @Override public void onProductClick(Product product) { addToCart(product, 1); }
    @Override public void onQuantityChange(Product product, int newQuantity) { if (newQuantity <= 0) removeFromCart(product); else updateCartQuantity(product, newQuantity); }

    private void addToCart(Product product, int addQty) {
        for (CartItem item : cartItems) {
            if (item.product.id == product.id) {
                item.quantity += addQty;
                updateProductQuantities();
                return;
            }
        }
        CartItem item = new CartItem(); item.product = product; item.quantity = addQty; cartItems.add(item);
        updateProductQuantities();
    }

    private void updateCartQuantity(Product product, int newQty) {
        for (CartItem item : cartItems) { if (item.product.id == product.id) { item.quantity = newQty; updateProductQuantities(); return; } }
        addToCart(product, newQty);
    }

    private void removeFromCart(Product product) {
        for (int i = 0; i < cartItems.size(); i++) { if (cartItems.get(i).product.id == product.id) { cartItems.remove(i); break; } }
        updateProductQuantities();
    }

    private void updateCart(){
        cartAdapter.setCartItems(cartItems);
        double total = 0; for(CartItem item : cartItems) total += item.getTotalPrice();
        totalAmount.setText("Total: ₹" + (int)total);
    }

    @Override public void onIncrease(CartItem item) { item.quantity++; updateCart(); updateProductQuantities(); }
    @Override public void onDecrease(CartItem item) {
        item.quantity--;
        if(item.quantity <= 0) { cartItems.remove(item); }
        updateCart(); updateProductQuantities();
    }
}