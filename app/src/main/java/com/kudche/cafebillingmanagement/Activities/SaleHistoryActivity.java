package com.kudche.cafebillingmanagement.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Repository.SaleRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SaleHistoryActivity extends AppCompatActivity {

    private SaleHistoryAdapter adapter;
    private SaleRepository saleRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_history);

        // Set up toolbar back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Sale History");
        }

        saleRepository = new SaleRepository(this);
        RecyclerView recyclerView = findViewById(R.id.saleHistoryRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("CafePrefs", MODE_PRIVATE);
        String role = prefs.getString("userRole", "WORKER");

        AppDatabase db = AppDatabase.getInstance(this);
        adapter = new SaleHistoryAdapter(db, saleRepository, role);
        recyclerView.setAdapter(adapter);

        db.saleDao().getAllSales().observe(this, sales -> {
            if (sales != null) {
                adapter.setSales(sales);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class SaleHistoryAdapter extends RecyclerView.Adapter<SaleHistoryAdapter.ViewHolder> {

        private List<Sale> sales = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        private final AppDatabase db;
        private final SaleRepository repository;
        private final String userRole;

        public SaleHistoryAdapter(AppDatabase db, SaleRepository repository, String role) {
            this.db = db;
            this.repository = repository;
            this.userRole = role;
        }

        public void setSales(List<Sale> sales) {
            this.sales = sales;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Sale sale = sales.get(position);
            holder.invoice.setText(sale.invoiceNumber != null ? sale.invoiceNumber : "INV-" + sale.id);
            holder.date.setText(dateFormat.format(new Date(sale.createdAt)));
            
            if (sale.isParcel) {
                holder.payment.setText("PARCEL");
                holder.payment.setTextColor(0xFF2196F3); // Blue
            } else {
                holder.payment.setText("DINE-IN");
                holder.payment.setTextColor(0xFF4CAF50); // Green
            }

            if (sale.isEmergencySale) {
                holder.payment.setText(holder.payment.getText() + " (STOCK BYPASS)");
                holder.payment.setTextColor(0xFFFF5722);
            }
            
            holder.total.setText("₹" + (int)sale.totalAmount);

            holder.expandIcon.setOnClickListener(v -> toggleDetails(holder, sale));
            holder.itemView.setOnClickListener(v -> toggleDetails(holder, sale));

            // Only OWNER can delete orders
            if ("OWNER".equals(userRole)) {
                holder.btnDeleteSale.setVisibility(View.VISIBLE);
                holder.btnDeleteSale.setOnClickListener(v -> {
                    new AlertDialog.Builder(holder.itemView.getContext())
                            .setTitle("Delete Order")
                            .setMessage("Are you sure you want to delete this order? This will revert the stock deductions.")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                repository.deleteSale(sale.id, () -> {
                                    holder.itemView.post(() -> Toast.makeText(holder.itemView.getContext(), "Order Deleted", Toast.LENGTH_SHORT).show());
                                });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            } else {
                holder.btnDeleteSale.setVisibility(View.GONE);
            }

            holder.detailsContainer.setVisibility(View.GONE);
            holder.expandIcon.setRotation(0);
        }

        private void toggleDetails(ViewHolder holder, Sale sale) {
            if (holder.detailsContainer.getVisibility() == View.VISIBLE) {
                holder.detailsContainer.setVisibility(View.GONE);
                holder.expandIcon.animate().rotation(0).start();
            } else {
                holder.detailsContainer.setVisibility(View.VISIBLE);
                holder.expandIcon.animate().rotation(180).start();
                loadSaleItems(holder, sale.id);
            }
        }

        private void loadSaleItems(ViewHolder holder, int saleId) {
            holder.itemsList.removeAllViews();
            Executors.newSingleThreadExecutor().execute(() -> {
                List<SaleItem> items = db.saleDao().getItemsForSale(saleId);
                for (SaleItem item : items) {
                    Product product = db.productDao().getByIdSync(item.productId);
                    holder.itemsList.post(() -> {
                        View itemView = LayoutInflater.from(holder.itemView.getContext())
                                .inflate(R.layout.item_sale_detail, holder.itemsList, false);
                        TextView name = itemView.findViewById(R.id.detailItemName);
                        TextView qty = itemView.findViewById(R.id.detailItemQty);
                        TextView unitPrice = itemView.findViewById(R.id.detailItemUnitPrice);
                        TextView totalPrice = itemView.findViewById(R.id.detailItemPrice);
                        
                        name.setText(product != null ? product.name : "Unknown Item");
                        qty.setText(String.valueOf(item.quantity));
                        unitPrice.setText("₹" + (int)item.priceAtSale);
                        totalPrice.setText("₹" + (int)(item.quantity * item.priceAtSale));

                        holder.itemsList.addView(itemView);
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return sales.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView invoice, date, payment, total;
            ImageView expandIcon;
            LinearLayout detailsContainer, itemsList;
            Button btnDeleteSale;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                invoice = itemView.findViewById(R.id.historyInvoice);
                date = itemView.findViewById(R.id.historyDate);
                payment = itemView.findViewById(R.id.historyPayment);
                total = itemView.findViewById(R.id.historyTotal);
                expandIcon = itemView.findViewById(R.id.expandIcon);
                detailsContainer = itemView.findViewById(R.id.detailsContainer);
                itemsList = itemView.findViewById(R.id.itemsList);
                btnDeleteSale = itemView.findViewById(R.id.btnDeleteSale);
            }
        }
    }
}