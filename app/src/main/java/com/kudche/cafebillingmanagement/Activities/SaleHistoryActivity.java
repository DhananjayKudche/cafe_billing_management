package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Dao.ProductDao;
import com.kudche.cafebillingmanagement.Dao.SaleDao;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SaleHistoryActivity extends AppCompatActivity {

    private SaleHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_history);

        RecyclerView recyclerView = findViewById(R.id.saleHistoryRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getInstance(this);
        adapter = new SaleHistoryAdapter(db);
        recyclerView.setAdapter(adapter);

        db.saleDao().getAllSales().observe(this, sales -> {
            if (sales != null) {
                adapter.setSales(sales);
            }
        });
    }

    private static class SaleHistoryAdapter extends RecyclerView.Adapter<SaleHistoryAdapter.ViewHolder> {

        private List<Sale> sales = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        private final AppDatabase db;

        public SaleHistoryAdapter(AppDatabase db) {
            this.db = db;
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
            holder.payment.setText(sale.paymentType);
            holder.total.setText("₹" + (int)sale.totalAmount);

            // Highlight emergency sales
            if (sale.isEmergencySale) {
                holder.total.setTextColor(0xFFFF5722); // Orange/Red
                holder.payment.setText(sale.paymentType + " (STOCK BYPASS)");
            } else {
                holder.total.setTextColor(0xFF4CAF50); // Green
                holder.payment.setText(sale.paymentType);
            }

            // Toggle expansion
            holder.expandIcon.setOnClickListener(v -> toggleDetails(holder, sale));
            holder.itemView.setOnClickListener(v -> toggleDetails(holder, sale));

            // Initial state
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
                        TextView price = itemView.findViewById(R.id.detailItemPrice);

                        name.setText(product != null ? product.name : "Unknown Item");
                        qty.setText(String.valueOf(item.quantity));
                        price.setText("₹" + (int)(item.quantity * item.priceAtSale));

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

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                invoice = itemView.findViewById(R.id.historyInvoice);
                date = itemView.findViewById(R.id.historyDate);
                payment = itemView.findViewById(R.id.historyPayment);
                total = itemView.findViewById(R.id.historyTotal);
                expandIcon = itemView.findViewById(R.id.expandIcon);
                detailsContainer = itemView.findViewById(R.id.detailsContainer);
                itemsList = itemView.findViewById(R.id.itemsList);
            }
        }
    }
}