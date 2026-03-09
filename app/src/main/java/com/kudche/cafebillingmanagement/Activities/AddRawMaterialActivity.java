package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Utils.ToastUtils;
import com.kudche.cafebillingmanagement.Utils.UnitConverter;
import com.kudche.cafebillingmanagement.ViewModel.RawMaterialViewModel;

public class AddRawMaterialActivity extends AppCompatActivity {

    RawMaterialViewModel viewModel;

    EditText nameInput;
    Spinner unitSpinner;
    EditText stockInput;

    int materialId = -1;
    String[] displayUnits = {
            UnitConverter.UNIT_GRAM, 
            UnitConverter.UNIT_KG, 
            UnitConverter.UNIT_ML, 
            UnitConverter.UNIT_LITER, 
            UnitConverter.UNIT_PCS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_raw_material);

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(RawMaterialViewModel.class);

        nameInput = findViewById(R.id.rawName);
        unitSpinner = findViewById(R.id.rawUnit);
        stockInput = findViewById(R.id.rawStock);
        TextView headerTitle = findViewById(R.id.headerTitle);

        Button addBtn = findViewById(R.id.addRawBtn);
        Button cancelBtn = findViewById(R.id.cancelBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayUnits);
        unitSpinner.setAdapter(adapter);

        materialId = getIntent().getIntExtra("materialId", -1);

        if(materialId != -1){
            headerTitle.setText("Edit Raw Material");
            new Thread(() -> {
                RawMaterial material = viewModel.getByIdSync(materialId);
                runOnUiThread(() -> {
                    if(material != null){
                        nameInput.setText(material.name);
                        stockInput.setText(String.valueOf(material.currentStock));
                        String baseUnit = material.unit;
                        for(int i = 0; i < displayUnits.length; i++){
                            if(displayUnits[i].equals(baseUnit)){
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
            String selectedUnit = unitSpinner.getSelectedItem().toString();
            String stockStr = stockInput.getText().toString().trim();

            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(stockStr)){
                ToastUtils.showInfo(this, "Please fill all fields");
                return;
            }

            double inputValue = Double.parseDouble(stockStr);
            double baseStock = UnitConverter.convertToBaseUnit(inputValue, selectedUnit);
            String baseUnitName = UnitConverter.getBaseUnit(selectedUnit);

            RawMaterial material = new RawMaterial();
            material.name = name;
            material.unit = baseUnitName; 
            material.currentStock = baseStock;

            if(materialId == -1){
                viewModel.insert(material);
                ToastUtils.showSuccess(this, "Raw material added");
            }else{
                material.id = materialId;
                viewModel.update(material);
                ToastUtils.showSuccess(this, "Raw material updated");
            }
            finish();
        });

        cancelBtn.setOnClickListener(v -> finish());
    }
}