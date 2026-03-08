package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.BulkOrder;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Utils.UnitConverter;
import com.kudche.cafebillingmanagement.ViewModel.RawMaterialViewModel;

import java.util.List;

public class AddRawMaterialActivity extends AppCompatActivity {

    RawMaterialViewModel viewModel;

    EditText nameInput;
    Spinner unitSpinner;
    EditText stockInput;

    int materialId = -1;
    String[] units = {"GRAM","ML","LITRE","KG","QUANTITY"};
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_raw_material);

        db = AppDatabase.getInstance(this);
        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(RawMaterialViewModel.class);

        nameInput = findViewById(R.id.rawName);
        unitSpinner = findViewById(R.id.rawUnit);
        stockInput = findViewById(R.id.rawStock);
        TextView headerTitle = findViewById(R.id.headerTitle);

        Button addBtn = findViewById(R.id.addRawBtn);
        Button cancelBtn = findViewById(R.id.cancelBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units);
        unitSpinner.setAdapter(adapter);

        materialId = getIntent().getIntExtra("materialId", -1);

        if(materialId != -1){
            headerTitle.setText("Edit Raw Material (Handover)");
            new Thread(() -> {
                RawMaterial material = viewModel.getByIdSync(materialId);
                runOnUiThread(() -> {
                    if(material != null){
                        nameInput.setText(material.name);
                        // Convert base unit to selected display unit
                        double displayStock = UnitConverter.convertFromBase(material.currentStock, material.unit);
                        stockInput.setText(String.valueOf(displayStock));
                        for(int i = 0; i < units.length; i++){
                            if(units[i].equalsIgnoreCase(material.unit)){
                                unitSpinner.setSelection(i);
                                break;
                            }
                        }
                    }
                });
            }).start();
        }

        addBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String unit = unitSpinner.getSelectedItem().toString();
            String stockStr = stockInput.getText().toString().trim();

            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(stockStr)){
                Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show();
                return;
            }

            double displayQty = Double.parseDouble(stockStr);
            // Convert everything to base unit for database storage
            double baseQty = UnitConverter.convertToBase(displayQty, unit);

            new Thread(() -> {
                if(materialId != -1) {
                    RawMaterial oldMaterial = viewModel.getByIdSync(materialId);
                    double differenceInBase = baseQty - oldMaterial.currentStock;
                    
                    if (differenceInBase > 0) {
                        // Deduct from Bulk Orders using base units
                        deductFromBulk(materialId, differenceInBase);
                    }
                    
                    oldMaterial.name = name;
                    oldMaterial.unit = unit;
                    oldMaterial.currentStock = baseQty;
                    viewModel.update(oldMaterial);
                } else {
                    RawMaterial material = new RawMaterial();
                    material.name = name;
                    material.unit = unit;
                    material.currentStock = baseQty;
                    viewModel.insert(material);
                }
                
                runOnUiThread(() -> {
                    Toast.makeText(this,"Inventory updated",Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });

        cancelBtn.setOnClickListener(v -> finish());
    }

    private void deductFromBulk(int rawId, double baseAmountToDeduct) {
        List<BulkOrder> bulkOrders = db.bulkOrderDao().getActiveBulkOrdersByMaterial(rawId);
        double remainingBaseToDeduct = baseAmountToDeduct;
        
        for (BulkOrder order : bulkOrders) {
            if (remainingBaseToDeduct <= 0) break;
            
            double orderRemainingInBase = UnitConverter.convertToBase(order.remainingInBulk, order.unit);
            
            if (orderRemainingInBase >= remainingBaseToDeduct) {
                orderRemainingInBase -= remainingBaseToDeduct;
                order.remainingInBulk = UnitConverter.convertFromBase(orderRemainingInBase, order.unit);
                db.bulkOrderDao().update(order);
                remainingBaseToDeduct = 0;
            } else {
                remainingBaseToDeduct -= orderRemainingInBase;
                order.remainingInBulk = 0;
                db.bulkOrderDao().update(order);
            }
        }
    }
}