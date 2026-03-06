package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.ViewModel.ProductViewModel;
import com.kudche.cafebillingmanagement.ViewModel.RawMaterialViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AddProductActivity extends AppCompatActivity {

    ProductViewModel viewModel;
    RawMaterialViewModel rawViewModel;

    EditText nameInput;
    LinearLayout materialContainer;
    Button addMaterialBtn;
    SeekBar priceSeekBar;
    TextView priceText;

    List<ProductRawMaterial> selectedMaterials = new ArrayList<>();
    List<RawMaterial> rawMaterials = new ArrayList<>();

    int productId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        rawViewModel = new ViewModelProvider(this).get(RawMaterialViewModel.class);

        nameInput = findViewById(R.id.productName);
        materialContainer = findViewById(R.id.materialContainer);
        addMaterialBtn = findViewById(R.id.addMaterialBtn);
        priceSeekBar = findViewById(R.id.priceSeekBar);
        priceText = findViewById(R.id.priceText);
        productId = getIntent().getIntExtra("productId",-1);

        if(productId != -1){
            loadProductData();
        }

        rawViewModel.getAll().observe(this, list -> rawMaterials = list);

        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                priceText.setText("₹ " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        addMaterialBtn.setOnClickListener(v -> showMaterialSelector());

        findViewById(R.id.saveProductBtn).setOnClickListener(v -> saveProduct());
        findViewById(R.id.cancelBtn).setOnClickListener(v -> finish());
    }

    private void loadProductData(){

        Executors.newSingleThreadExecutor().execute(() -> {

            Product product = viewModel.getProductById(productId);

            List<ProductRawMaterial> materials =
                    viewModel.getMaterialsByProduct(productId);

            runOnUiThread(() -> {

                nameInput.setText(product.name);

                priceSeekBar.setProgress((int) product.price);

                for(ProductRawMaterial m : materials){

//                    ProductRawMaterial raw = viewModel.getRawMaterialById(m.rawMaterialId);
                    viewModel.getRawMaterialById(m.rawMaterialId, raw -> {

                        addMaterialView(
                                raw.name,
                                m.quantityRequired,
                                raw.unit
                        );

                    });
                }
            });
        });
    }


    private void showMaterialSelector() {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_material, null);

        Spinner spinner = view.findViewById(R.id.rawMaterialSpinner);
        EditText qtyInput = view.findViewById(R.id.quantityInput);
        TextView unitText = view.findViewById(R.id.unitText);

        ArrayAdapter<RawMaterial> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        rawMaterials);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                unitText.setText(rawMaterials.get(position).unit);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Raw Material")
                .setView(view)
                .setPositiveButton("Add", (d, w) -> {

                    RawMaterial selected =
                            (RawMaterial) spinner.getSelectedItem();

                    double qty = Double.parseDouble(
                            qtyInput.getText().toString());

                    ProductRawMaterial mapping =
                            new ProductRawMaterial();

                    mapping.rawMaterialId = selected.id;
                    mapping.quantityRequired = qty;

                    selectedMaterials.add(mapping);

                    addMaterialView(selected.name,
                            qty,
                            selected.unit);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addMaterialView(String name,
                                 double qty,
                                 String unit){

        TextView textView = new TextView(this);
        textView.setText(name + " - " + qty + " " + unit);
        materialContainer.addView(textView);
    }

    private void saveProduct(){

        Product product = new Product();

        product.name = nameInput.getText().toString();
        product.price = priceSeekBar.getProgress();
        product.currentStock = 50;
        product.lowStockThreshold = 5;

        if(productId == -1){

            viewModel.insertProduct(product, selectedMaterials);

        }else{

            product.id = productId;

            viewModel.updateProduct(product, selectedMaterials);
        }

        finish();
    }
}