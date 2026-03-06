package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.DayClose;
import com.kudche.cafebillingmanagement.Models.DayCloseItem;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DayCloseActivity extends AppCompatActivity {

    private ReconcileAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_close);

        db = AppDatabase.getInstance(this);
        RecyclerView recyclerView = findViewById(R.id.reconcileRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReconcileAdapter();
        recyclerView.setAdapter(adapter);

        findViewById(R.id.submitDayCloseBtn).setOnClickListener(v -> submitDayClose());

        loadMaterials();
    }

    private void loadMaterials() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<RawMaterial> materials = db.rawMaterialDao().getAllSync();
            List<DayCloseItem> reconcileItems = new ArrayList<>();

            for (RawMaterial rm : materials) {
                DayCloseItem item = new DayCloseItem();
                item.rawMaterialId = rm.id;
                item.materialName = rm.name;
                item.expectedStock = rm.currentStock;
                item.actualStock = rm.currentStock; // Default to expected
                reconcileItems.add(item);
            }

            runOnUiThread(() -> adapter.setItems(reconcileItems));
        });
    }

    private void submitDayClose() {
        List<DayCloseItem> items = adapter.getItems();
        Executors.newSingleThreadExecutor().execute(() -> {
            db.runInTransaction(() -> {
                DayClose dc = new DayClose();
                dc.date = System.currentTimeMillis();
                dc.status = "SUBMITTED";
                
                long dcId = db.dayCloseDao().insert(dc);

                for (DayCloseItem item : items) {
                    item.dayCloseId = (int) dcId;
                    item.variance = item.actualStock - item.expectedStock;
                }
                db.dayCloseDao().insertItems(items);
            });

            runOnUiThread(() -> {
                Toast.makeText(this, "Day Close Submitted Successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private static class ReconcileAdapter extends RecyclerView.Adapter<ReconcileAdapter.ViewHolder> {
        private List<DayCloseItem> items = new ArrayList<>();

        public void setItems(List<DayCloseItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        public List<DayCloseItem> getItems() {
            return items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_close_reconcile, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DayCloseItem item = items.get(position);
            holder.name.setText(item.materialName);
            holder.expected.setText("Expected: " + item.expectedStock);
            holder.actual.setText(String.valueOf(item.actualStock));

            holder.actual.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    try {
                        item.actualStock = Double.parseDouble(s.toString());
                    } catch (Exception e) {
                        item.actualStock = 0;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, expected;
            EditText actual;
            public ViewHolder(@NonNull View v) {
                super(v);
                name = v.findViewById(R.id.reconcileMaterialName);
                expected = v.findViewById(R.id.reconcileExpectedText);
                actual = v.findViewById(R.id.reconcileActualInput);
            }
        }
    }
}