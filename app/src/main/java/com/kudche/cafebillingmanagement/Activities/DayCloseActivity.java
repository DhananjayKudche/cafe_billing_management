package com.kudche.cafebillingmanagement.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.DayClose;
import com.kudche.cafebillingmanagement.Models.DayCloseItem;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Utils.ToastUtils;
import com.kudche.cafebillingmanagement.Utils.UnitConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class DayCloseActivity extends AppCompatActivity {

    private ReconcileAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_close);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> showLogoutDialog());

        db = AppDatabase.getInstance(this);
        RecyclerView recyclerView = findViewById(R.id.reconcileRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReconcileAdapter();
        recyclerView.setAdapter(adapter);

        findViewById(R.id.submitDayCloseBtn).setOnClickListener(v -> submitDayClose());

        loadMaterials();
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

    private void loadMaterials() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<RawMaterial> materials = db.rawMaterialDao().getAllSync();
            List<DayCloseItem> reconcileItems = new ArrayList<>();
            Map<Integer, String> unitMap = new HashMap<>();

            for (RawMaterial rm : materials) {
                DayCloseItem item = new DayCloseItem();
                item.rawMaterialId = rm.id;
                item.materialName = rm.name;
                item.expectedStock = rm.currentStock;
                item.actualStock = rm.currentStock; // Default to expected
                reconcileItems.add(item);
                unitMap.put(rm.id, rm.unit);
            }

            runOnUiThread(() -> {
                adapter.setUnits(unitMap);
                adapter.setItems(reconcileItems);
            });
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
                ToastUtils.showSuccess(this, "Day Close Submitted Successfully");
                finish();
            });
        });
    }

    private static class ReconcileAdapter extends RecyclerView.Adapter<ReconcileAdapter.ViewHolder> {
        private List<DayCloseItem> items = new ArrayList<>();
        private Map<Integer, String> unitMap = new HashMap<>();

        public void setItems(List<DayCloseItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        public void setUnits(Map<Integer, String> units) {
            this.unitMap = units;
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
            
            String baseUnit = unitMap.get(item.rawMaterialId);
            if (baseUnit == null) baseUnit = "";

            double displayValue = item.expectedStock;
            String displayUnit = baseUnit;

            // Logical Display for System Stock
            if (UnitConverter.UNIT_KG.equals(baseUnit) && item.expectedStock < 1.0) {
                displayValue = UnitConverter.convertFromBaseUnit(item.expectedStock, UnitConverter.UNIT_GRAM);
                displayUnit = UnitConverter.UNIT_GRAM;
            } else if (UnitConverter.UNIT_LITER.equals(baseUnit) && item.expectedStock < 1.0) {
                displayValue = UnitConverter.convertFromBaseUnit(item.expectedStock, UnitConverter.UNIT_ML);
                displayUnit = UnitConverter.UNIT_ML;
            }

            holder.expected.setText(String.format(Locale.getDefault(), "%.2f %s", displayValue, displayUnit));
            
            holder.actual.setText(String.format(Locale.getDefault(), "%.2f", displayValue));
            
            final String finalDisplayUnit = displayUnit;
            holder.actual.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    try {
                        double val = Double.parseDouble(s.toString());
                        item.actualStock = UnitConverter.convertToBaseUnit(val, finalDisplayUnit);
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