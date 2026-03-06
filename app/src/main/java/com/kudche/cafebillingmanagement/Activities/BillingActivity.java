package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Adapters.ProductAdapter;
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
import java.util.List;

public class BillingActivity extends AppCompatActivity
        implements BillingProductAdapter.ProductClickListener,
        CartAdapter.CartActionListener {

    private List<CartItem> cartItems = new ArrayList<>();

    private SaleRepository saleRepository;
    private CartAdapter cartAdapter;
//    private ProductAdapter productAdapter;

    BillingProductAdapter productAdapter;

    private TextView totalAmount;

    private ProductViewModel productViewModel;

    private AppDatabase db;

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

        // PRODUCT ADAPTER
        productAdapter = new BillingProductAdapter(this);

        productRecycler.setLayoutManager(new GridLayoutManager(this,3));
        productRecycler.setAdapter(productAdapter);

        // CART ADAPTER
        cartAdapter = new CartAdapter(this);

        cartRecycler.setLayoutManager(new LinearLayoutManager(this));
        cartRecycler.setAdapter(cartAdapter);

        // VIEWMODEL
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        productViewModel.getProducts().observe(this, products -> {
            productAdapter.setProducts(products);
        });

        // COMPLETE SALE
        completeSaleBtn.setOnClickListener(v -> {

            if(cartItems.isEmpty()){
                Toast.makeText(this,"Cart Empty",Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {

                List<SaleItem> saleItems = new ArrayList<>();

                for(CartItem cart : cartItems){

                    boolean stockOk = StockManager.isStockAvailable(
                            db,
                            cart.product.id,
                            cart.quantity
                    );

                    if(!stockOk){

                        runOnUiThread(() ->
                                Toast.makeText(
                                        BillingActivity.this,
                                        "Insufficient stock for " + cart.product.name,
                                        Toast.LENGTH_LONG
                                ).show()
                        );

                        return;
                    }

                    SaleItem item = new SaleItem();
                    item.productId = cart.product.id;
                    item.quantity = cart.quantity;

                    saleItems.add(item);
                }

                saleRepository.createSale(saleItems);

                runOnUiThread(() -> {

                    Toast.makeText(
                            BillingActivity.this,
                            "Sale Completed",
                            Toast.LENGTH_SHORT
                    ).show();

                    cartItems.clear();
                    updateCart();
                });

            }).start();

        });

    }

    // PRODUCT CLICK
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
        item.quantity = 1;

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

    // CART INCREASE
    @Override
    public void onIncrease(CartItem item) {
        item.quantity++;
        updateCart();
    }

    // CART DECREASE
    @Override
    public void onDecrease(CartItem item) {

        item.quantity--;

        if(item.quantity <= 0){
            cartItems.remove(item);
        }

        updateCart();
    }

}