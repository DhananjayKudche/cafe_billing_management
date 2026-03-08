package com.kudche.cafebillingmanagement.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.BulkOrder;
import com.kudche.cafebillingmanagement.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BulkOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BulkOrderAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_order);

        db = AppDatabase.getInstance(this);
        recyclerView = findViewById(R.id.bulkOrderRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new BulkOrderAdapter();
        recyclerView.setAdapter(adapter);

        db.bulkOrderDao().getAllBulkOrders().observe(this, bulkOrders -> {
            adapter.setList(bulkOrders);
        });

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        
        FloatingActionButton fab = findViewById(R.id.fabAddBulkOrder);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, AddBulkOrderActivity.class));
        });
    }

    private class BulkOrderAdapter extends RecyclerView.Adapter<BulkOrderAdapter.ViewHolder> {
        private List<BulkOrder> list = new ArrayList<>();

        public void setList(List<BulkOrder> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BulkOrder order = list.get(position);
            holder.tvName.setText(order.rawMaterialName);
            holder.tvQty.setText("Total: " + order.totalQuantity + " " + order.unit);
            holder.tvRemaining.setText("In Bulk Storage: " + order.remainingInBulk + " " + order.unit);
            holder.tvDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(order.date)));
            
            holder.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(BulkOrderActivity.this, AddBulkOrderActivity.class);
                intent.putExtra("bulkOrderId", order.id);
                startActivity(intent);
            });

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(BulkOrderActivity.this)
                        .setTitle("Delete Bulk Order")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (d, w) -> {
                            new Thread(() -> {
                                db.bulkOrderDao().delete(order);
                            }).start();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvQty, tvRemaining, tvDate;
            ImageButton btnEdit;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMaterialName);
                tvQty = itemView.findViewById(R.id.tvTotalQty);
                tvRemaining = itemView.findViewById(R.id.tvRemainingQty);
                tvDate = itemView.findViewById(R.id.tvOrderDate);
                btnEdit = itemView.findViewById(R.id.btnEditBulkOrder);
            }
        }
    }
}