package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Adapters.Sale.BillingProductAdapter;
import com.kudche.cafebillingmanagement.Adapters.Sale.CartAdapter;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.CartItem;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Repository.SaleRepository;
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
    private List<Product> allProducts = new ArrayList<>();

    private int productPage = 0;
    private final int PRODUCT_PAGE_SIZE = 6;

    private int cartPage = 0;
    private final int CART_PAGE_SIZE = 4;

    private SaleRepository saleRepository;
    private CartAdapter cartAdapter;
    private BillingProductAdapter productAdapter;

    private TextView totalAmount;
    private ProductViewModel productViewModel;
    private AppDatabase db;

    private Button prevProductBtn, nextProductBtn, prevCartBtn, nextCartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        db = AppDatabase.getInstance(this);
        saleRepository = new SaleRepository(this);

        RecyclerView productRecycler = findViewById(R.id.productRecycler);
        RecyclerView cartRecycler = findViewById(R.id.cartRecycler);
        totalAmount = findViewById(R.id.totalAmount);

        Button completeSaleBtn = findViewById(R.id.completeSale);
        Button cancelSaleBtn = findViewById(R.id.cancelSaleBtn);
        prevProductBtn = findViewById(R.id.prevProductBtn);
        nextProductBtn = findViewById(R.id.nextProductBtn);
        prevCartBtn = findViewById(R.id.prevCartBtn);
        nextCartBtn = findViewById(R.id.nextCartBtn);

        productAdapter = new BillingProductAdapter(this);
        productRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        productRecycler.setAdapter(productAdapter);

        cartAdapter = new CartAdapter(this);
        cartRecycler.setLayoutManager(new LinearLayoutManager(this));
        cartRecycler.setAdapter(cartAdapter);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productViewModel.getProducts().observe(this, products -> {
            allProducts = products;
            updateProductList();
        });

        prevProductBtn.setOnClickListener(v -> { if (productPage > 0) { productPage--; updateProductList(); } });
        nextProductBtn.setOnClickListener(v -> { if ((productPage + 1) * PRODUCT_PAGE_SIZE < allProducts.size()) { productPage++; updateProductList(); } });
        prevCartBtn.setOnClickListener(v -> { if (cartPage > 0) { cartPage--; updateCart(); } });
        nextCartBtn.setOnClickListener(v -> { if ((cartPage + 1) * CART_PAGE_SIZE < cartItems.size()) { cartPage++; updateCart(); } });

        cancelSaleBtn.setOnClickListener(v -> {
            cartItems.clear();
            cartPage = 0;
            updateCart();
            updateProductQuantities();
            Toast.makeText(this, "Sale Cancelled", Toast.LENGTH_SHORT).show();
        });

        completeSaleBtn.setOnClickListener(v -> checkStockAndProceed());
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

        saleRepository.createSale(saleItems, isEmergency);

        runOnUiThread(() -> {
            Toast.makeText(this, isEmergency ? "Emergency Sale Recorded" : "Sale Completed", Toast.LENGTH_SHORT).show();
            cartItems.clear();
            cartPage = 0;
            updateCart();
            updateProductQuantities();
        });
    }

    private void updateProductList() {
        int start = productPage * PRODUCT_PAGE_SIZE;
        int end = Math.min(start + PRODUCT_PAGE_SIZE, allProducts.size());
        if (start < allProducts.size()) productAdapter.setProducts(allProducts.subList(start, end));
        else productAdapter.setProducts(new ArrayList<>());
        prevProductBtn.setVisibility(allProducts.size() > PRODUCT_PAGE_SIZE ? View.VISIBLE : View.GONE);
        nextProductBtn.setVisibility(allProducts.size() > PRODUCT_PAGE_SIZE ? View.VISIBLE : View.GONE);
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
                updateCart(); updateProductQuantities();
                return;
            }
        }
        CartItem item = new CartItem(); item.product = product; item.quantity = addQty; cartItems.add(item);
        cartPage = (cartItems.size() - 1) / CART_PAGE_SIZE;
        updateCart(); updateProductQuantities();
    }

    private void updateCartQuantity(Product product, int newQty) {
        for (CartItem item : cartItems) { if (item.product.id == product.id) { item.quantity = newQty; updateCart(); updateProductQuantities(); return; } }
        addToCart(product, newQty);
    }

    private void removeFromCart(Product product) {
        for (int i = 0; i < cartItems.size(); i++) { if (cartItems.get(i).product.id == product.id) { cartItems.remove(i); break; } }
        if (cartPage * CART_PAGE_SIZE >= cartItems.size() && cartPage > 0) cartPage--;
        updateCart(); updateProductQuantities();
    }

    private void updateCart(){
        int start = cartPage * CART_PAGE_SIZE;
        int end = Math.min(start + CART_PAGE_SIZE, cartItems.size());
        if (start < cartItems.size()) cartAdapter.setCartItems(cartItems.subList(start, end));
        else cartAdapter.setCartItems(new ArrayList<>());
        prevCartBtn.setVisibility(cartItems.size() >= CART_PAGE_SIZE ? View.VISIBLE : View.GONE);
        nextCartBtn.setVisibility(cartItems.size() >= CART_PAGE_SIZE ? View.VISIBLE : View.GONE);
        double total = 0; for(CartItem item : cartItems) total += item.getTotalPrice();
        totalAmount.setText("Total: ₹" + (int)total);
    }

    @Override public void onIncrease(CartItem item) { item.quantity++; updateCart(); updateProductQuantities(); }
    @Override public void onDecrease(CartItem item) {
        item.quantity--;
        if(item.quantity <= 0) { cartItems.remove(item); if (cartPage * CART_PAGE_SIZE >= cartItems.size() && cartPage > 0) cartPage--; }
        updateCart(); updateProductQuantities();
    }
}