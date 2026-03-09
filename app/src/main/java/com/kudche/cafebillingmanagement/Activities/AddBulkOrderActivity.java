package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.BulkOrder;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;

import java.util.ArrayList;
import java.util.List;

public class AddBulkOrderActivity extends AppCompatActivity {

    private Spinner materialSpinner, unitSpinner;
    private EditText etBulkQty, etSupplier;
    private AppDatabase db;
    private List<RawMaterial> rawMaterials = new ArrayList<>();
    private int bulkOrderId = -1;
    private String[] units = {"KG", "GRAM", "LITRE", "ML", "QUANTITY"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bulk_order);

        db = AppDatabase.getInstance(this);
        materialSpinner = findViewById(R.id.materialSpinner);
        unitSpinner = findViewById(R.id.unitSpinner);
        etBulkQty = findViewById(R.id.etBulkQty);
        etSupplier = findViewById(R.id.etSupplier);
        TextView headerTitle = findViewById(R.id.headerTitle);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units);
        unitSpinner.setAdapter(unitAdapter);

        bulkOrderId = getIntent().getIntExtra("bulkOrderId", -1);

        loadMaterials();

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveBulkOrder).setOnClickListener(v -> saveBulkOrder());
        
        if (bulkOrderId != -1) {
            headerTitle.setText("Edit Bulk Order");
            loadBulkOrder();
        }
    }

    private void loadMaterials() {
        new Thread(() -> {
            rawMaterials = db.rawMaterialDao().getAllSync();
            runOnUiThread(() -> {
                ArrayAdapter<RawMaterial> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item, rawMaterials);
                materialSpinner.setAdapter(adapter);
                
                // If materials are loaded after bulk order, we need to select it
                if (bulkOrderId != -1) {
                    selectCurrentMaterial();
                }
            });
        }).start();
    }

    private void selectCurrentMaterial() {
        new Thread(() -> {
            BulkOrder order = db.bulkOrderDao().getById(bulkOrderId);
            if (order != null) {
                runOnUiThread(() -> {
                    for (int i = 0; i < rawMaterials.size(); i++) {
                        if (rawMaterials.get(i).id == order.rawMaterialId) {
                            materialSpinner.setSelection(i);
                            break;
                        }
                    }
                });
            }
        }).start();
    }

    private void loadBulkOrder() {
        new Thread(() -> {
            BulkOrder order = db.bulkOrderDao().getById(bulkOrderId);
            runOnUiThread(() -> {
                if (order != null) {
                    etBulkQty.setText(String.valueOf(order.totalQuantity));
                    etSupplier.setText(order.supplierName);
                    
                    for (int i = 0; i < units.length; i++) {
                        if (units[i].equalsIgnoreCase(order.unit)) {
                            unitSpinner.setSelection(i);
                            break;
                        }
                    }
                    // Selection of material is handled in loadMaterials/selectCurrentMaterial
                }
            });
        }).start();
    }

    private void saveBulkOrder() {
        RawMaterial selected = (RawMaterial) materialSpinner.getSelectedItem();
        String qtyStr = etBulkQty.getText().toString();
        String supplier = etSupplier.getText().toString();
        String unit = unitSpinner.getSelectedItem().toString();

        if (selected == null || qtyStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double newTotalQty = Double.parseDouble(qtyStr);

        new Thread(() -> {
            BulkOrder order;
            if (bulkOrderId == -1) {
                order = new BulkOrder();
                order.rawMaterialId = selected.id;
                order.rawMaterialName = selected.name;
                order.totalQuantity = newTotalQty;
                order.remainingInBulk = newTotalQty;
                order.unit = unit;
                order.supplierName = supplier;
                order.date = System.currentTimeMillis();
                db.bulkOrderDao().insert(order);
            } else {
                order = db.bulkOrderDao().getById(bulkOrderId);
                double diff = newTotalQty - order.totalQuantity;
                
                order.rawMaterialId = selected.id;
                order.rawMaterialName = selected.name;
                order.totalQuantity = newTotalQty;
                // Adjust remaining based on difference in total
                order.remainingInBulk += diff;
                if (order.remainingInBulk < 0) order.remainingInBulk = 0;
                
                order.unit = unit;
                order.supplierName = supplier;
                db.bulkOrderDao().update(order);
            }
            
            runOnUiThread(() -> {
                Toast.makeText(this, bulkOrderId == -1 ? "Bulk Order Added" : "Bulk Order Updated", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}