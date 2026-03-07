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

import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.ViewModel.RawMaterialViewModel;

public class AddRawMaterialActivity extends AppCompatActivity {

    RawMaterialViewModel viewModel;

    EditText nameInput;
    Spinner unitSpinner;
    EditText stockInput;

    int materialId = -1;
    RawMaterial existingMaterial;

    String[] units = {"GRAM","ML","LITRE","KG","QUANTITY"};

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

        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                units
        );

        unitSpinner.setAdapter(adapter);

        // Check if edit mode
        materialId = getIntent().getIntExtra("materialId", -1);

        if(materialId != -1){
            headerTitle.setText("Edit Raw Material");
            new Thread(() -> {
                RawMaterial material = viewModel.getByIdSync(materialId);
                runOnUiThread(() -> {
                    if(material != null){
                        nameInput.setText(material.name);
                        stockInput.setText(String.valueOf(material.currentStock));
                        for(int i = 0; i < units.length; i++){
                            if(units[i].equals(material.unit)){
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

            double stock = Double.parseDouble(stockStr);

            RawMaterial material = new RawMaterial();
            material.name = name;
            material.unit = unit;
            material.currentStock = stock;

            if(materialId == -1){
                viewModel.insert(material);
                Toast.makeText(this,"Raw material added",Toast.LENGTH_SHORT).show();
            }else{
                material.id = materialId;
                viewModel.update(material);
                Toast.makeText(this,"Raw material updated",Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        cancelBtn.setOnClickListener(v -> finish());
    }
}