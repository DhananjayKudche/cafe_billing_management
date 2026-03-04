package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Adapters.ProductAdapter;
import com.kudche.cafebillingmanagement.Adapters.Sale.CartAdapter;
import com.kudche.cafebillingmanagement.Models.CartItem;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.ViewModel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

public class BillingActivity extends AppCompatActivity
        implements ProductAdapter.ProductClickListener,
        CartAdapter.CartActionListener {
    private List<CartItem> cartItems = new ArrayList<>();

    private CartAdapter cartAdapter;

    private TextView totalAmount;

    private ProductViewModel productViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        RecyclerView productRecycler = findViewById(R.id.productRecycler);
        RecyclerView cartRecycler = findViewById(R.id.cartRecycler);

        totalAmount = findViewById(R.id.totalAmount);

        ProductAdapter productAdapter = new ProductAdapter(this);

        productRecycler.setLayoutManager(new GridLayoutManager(this,3));
        productRecycler.setAdapter(productAdapter);

        cartAdapter = new CartAdapter(this);
        cartRecycler.setLayoutManager(new LinearLayoutManager(this));
        cartRecycler.setAdapter(cartAdapter);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        productViewModel.getProducts().observe(this, products -> {
            productAdapter.setProducts(products);
        });
    }

    @Override
    public void onProductClick(Product product) {

        for(CartItem item : cartItems){
            if(item.product.id == product.id){
                item.quantity++;
                updateCart();
                return;
            }
        }

        CartItem item = new CartItem();
        item.product = product;

        cartItems.add(item);

        updateCart();
    }

    private void updateCart(){

        cartAdapter.setCartItems(cartItems);

        double total = 0;

        for(CartItem item : cartItems){
            total += item.getTotalPrice();
        }

        totalAmount.setText("Total: ₹" + total);
    }

    @Override
    public void onIncrease(CartItem item) {
        item.quantity++;
        updateCart();
    }

    @Override
    public void onDecrease(CartItem item) {
        item.quantity--;

        if(item.quantity <= 0){
            cartItems.remove(item);
        }

        updateCart();
    }
}