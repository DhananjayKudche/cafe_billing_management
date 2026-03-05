package com.kudche.cafebillingmanagement.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kudche.cafebillingmanagement.Adapters.RawMaterialAdapter;
import com.kudche.cafebillingmanagement.R;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.ViewModel.RawMaterialViewModel;import androidx.appcompat.app.AlertDialog;

public class RawMaterialActivity extends AppCompatActivity {

    RawMaterialViewModel viewModel;
    RecyclerView recyclerView;
    FloatingActionButton fab;
    RawMaterialAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_material);

        viewModel = new ViewModelProvider(this).get(RawMaterialViewModel.class);

        recyclerView = findViewById(R.id.rawRecycler);
        fab = findViewById(R.id.fabAddRaw);

        adapter = new RawMaterialAdapter(

                material -> {

                    new AlertDialog.Builder(this)
                            .setTitle("Delete Raw Material")
                            .setMessage("Are you sure you want to delete " + material.name + "?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                viewModel.delete(material);
                                Toast.makeText(this, material.name + " deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                },

                material -> {
                    Intent intent = new Intent(this, AddRawMaterialActivity.class);
                    intent.putExtra("materialId", material.id);
                    startActivity(intent);
                }

        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel.getAll().observe(this, materials -> {
            adapter.setList(materials);
        });

        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, AddRawMaterialActivity.class));
        });
    }
}