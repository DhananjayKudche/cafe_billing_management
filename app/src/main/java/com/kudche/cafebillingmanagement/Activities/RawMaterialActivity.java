package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kudche.cafebillingmanagement.R;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.ViewModel.RawMaterialViewModel;

public class RawMaterialActivity extends AppCompatActivity {

    RawMaterialViewModel viewModel;

    EditText nameInput;
    Spinner unitSpinner;
    EditText stockInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_material);

        viewModel = new ViewModelProvider(this).get(RawMaterialViewModel.class);

        nameInput = findViewById(R.id.rawName);
        unitSpinner = findViewById(R.id.rawUnit);

        String[] units = {"GRAM", "ML"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                units
        );

        unitSpinner.setAdapter(adapter);
        stockInput = findViewById(R.id.rawStock);

        Button addBtn = findViewById(R.id.addRawBtn);

        addBtn.setOnClickListener(v -> {

            RawMaterial material = new RawMaterial();

            material.name = nameInput.getText().toString();
            material.unit = unitSpinner.getSelectedItem().toString();
            material.currentStock = Double.parseDouble(stockInput.getText().toString());

            viewModel.insert(material);

            Toast.makeText(this,"Raw material added",Toast.LENGTH_SHORT).show();
        });
    }
}