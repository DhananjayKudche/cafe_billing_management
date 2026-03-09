package com.kudche.cafebillingmanagement.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Utils.UnitConverter;
import com.kudche.cafebillingmanagement.ViewModel.ProductViewModel;
import com.kudche.cafebillingmanagement.ViewModel.RawMaterialViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class AddProductActivity extends AppCompatActivity {

    ProductViewModel viewModel;
    RawMaterialViewModel rawViewModel;

    EditText nameInput;
    EditText priceInput;
    LinearLayout materialContainer;
    Button addMaterialBtn;
    ImageView productImage;
    Button selectImageBtn;
    Spinner categorySpinner;

    List<ProductRawMaterial> selectedMaterials = new ArrayList<>();
    List<RawMaterial> rawMaterials = new ArrayList<>();

    int productId = -1;
    String currentImagePath = null;
    String[] categories = {"Cafe Category", "Juice Category"};

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveImageToInternalStorage(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        rawViewModel = new ViewModelProvider(this).get(RawMaterialViewModel.class);

        nameInput = findViewById(R.id.productName);
        priceInput = findViewById(R.id.productPrice);
        materialContainer = findViewById(R.id.materialContainer);
        addMaterialBtn = findViewById(R.id.addMaterialBtn);
        productImage = findViewById(R.id.productImage);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        categorySpinner = findViewById(R.id.categorySpinner);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(catAdapter);

        productId = getIntent().getIntExtra("productId",-1);

        if(productId != -1){
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Product");
            }
            loadProductData();
        }

        rawViewModel.getAll().observe(this, list -> {
            rawMaterials = list;
            refreshMaterialList();
        });

        addMaterialBtn.setOnClickListener(v -> showMaterialSelector(null));
        selectImageBtn.setOnClickListener(v -> openImagePicker());

        findViewById(R.id.saveProductBtn).setOnClickListener(v -> saveProduct());
        findViewById(R.id.cancelBtn).setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String fileName = UUID.randomUUID().toString() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            currentImagePath = file.getAbsolutePath();
            productImage.setImageURI(Uri.fromFile(file));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductData(){
        Executors.newSingleThreadExecutor().execute(() -> {
            Product product = viewModel.getProductById(productId);
            List<ProductRawMaterial> materials = viewModel.getMaterialsByProduct(productId);

            runOnUiThread(() -> {
                nameInput.setText(product.name);
                priceInput.setText(String.valueOf(product.price));
                currentImagePath = product.imagePath;
                
                // Set Category
                if (product.category != null) {
                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i].equals(product.category)) {
                            categorySpinner.setSelection(i);
                            break;
                        }
                    }
                }

                if (currentImagePath != null) {
                    if (currentImagePath.startsWith("ic_")) {
                        int resId = getResources().getIdentifier(currentImagePath, "drawable", getPackageName());
                        if (resId != 0) productImage.setImageResource(resId);
                    } else {
                        productImage.setImageURI(Uri.fromFile(new File(currentImagePath)));
                    }
                }

                selectedMaterials.clear();
                selectedMaterials.addAll(materials);
                refreshMaterialList();
            });
        });
    }

    private void refreshMaterialList() {
        materialContainer.removeAllViews();
        for (ProductRawMaterial mapping : selectedMaterials) {
            RawMaterial raw = null;
            for (RawMaterial rm : rawMaterials) {
                if (rm.id == mapping.rawMaterialId) {
                    raw = rm;
                    break;
                }
            }
            addMaterialView(mapping, raw);
        }
    }

    private void showMaterialSelector(ProductRawMaterial existingMapping) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_material, null);
        Spinner rawSpinner = view.findViewById(R.id.rawMaterialSpinner);
        EditText qtyInput = view.findViewById(R.id.quantityInput);
        Spinner unitSpinner = view.findViewById(R.id.unitSpinner);

        ArrayAdapter<RawMaterial> rawAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rawMaterials);
        rawSpinner.setAdapter(rawAdapter);

        rawSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RawMaterial selected = rawMaterials.get(position);
                String baseUnit = UnitConverter.getBaseUnit(selected.unit);
                String[] relatedUnits = UnitConverter.getRelatedUnits(baseUnit);
                
                ArrayAdapter<String> uAdapter = new ArrayAdapter<>(AddProductActivity.this, android.R.layout.simple_spinner_dropdown_item, relatedUnits);
                unitSpinner.setAdapter(uAdapter);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (existingMapping != null) {
            for (int i = 0; i < rawMaterials.size(); i++) {
                if (rawMaterials.get(i).id == existingMapping.rawMaterialId) {
                    rawSpinner.setSelection(i);
                    // Qty is stored in base unit, so we display it in base unit initially
                    qtyInput.setText(String.valueOf(existingMapping.quantityRequired));
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(existingMapping == null ? "Add Raw Material" : "Edit Raw Material")
                .setView(view)
                .setPositiveButton(existingMapping == null ? "Add" : "Update", (d, w) -> {
                    RawMaterial selected = (RawMaterial) rawSpinner.getSelectedItem();
                    String qtyStr = qtyInput.getText().toString();
                    String selectedUnit = unitSpinner.getSelectedItem().toString();
                    
                    if(qtyStr.isEmpty()) return;

                    double qty = Double.parseDouble(qtyStr);
                    // Convert to base unit for storage
                    double baseQty = UnitConverter.convertToBaseUnit(qty, selectedUnit);

                    if (existingMapping == null) {
                        ProductRawMaterial mapping = new ProductRawMaterial();
                        mapping.rawMaterialId = selected.id;
                        mapping.quantityRequired = baseQty;
                        selectedMaterials.add(mapping);
                    } else {
                        existingMapping.rawMaterialId = selected.id;
                        existingMapping.quantityRequired = baseQty;
                    }
                    refreshMaterialList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addMaterialView(ProductRawMaterial mapping, RawMaterial raw){
        View view = LayoutInflater.from(this).inflate(R.layout.item_selected_material, materialContainer, false);

        TextView nameText = view.findViewById(R.id.materialNameText);
        ImageView editIcon = view.findViewById(R.id.editMaterialIcon);
        ImageView deleteIcon = view.findViewById(R.id.deleteMaterialIcon);

        String displayText;
        if (raw != null) {
            String baseUnit = UnitConverter.getBaseUnit(raw.unit);
            double displayValue = mapping.quantityRequired;
            String displayUnit = baseUnit;
            
            // Logic to show in smaller unit if it's small (e.g. 0.05 KG -> 50 GRAM)
            if (baseUnit.equals(UnitConverter.UNIT_KG) && mapping.quantityRequired < 1.0) {
                displayValue = UnitConverter.convertFromBaseUnit(mapping.quantityRequired, UnitConverter.UNIT_GRAM);
                displayUnit = UnitConverter.UNIT_GRAM;
            } else if (baseUnit.equals(UnitConverter.UNIT_LITER) && mapping.quantityRequired < 1.0) {
                displayValue = UnitConverter.convertFromBaseUnit(mapping.quantityRequired, UnitConverter.UNIT_ML);
                displayUnit = UnitConverter.UNIT_ML;
            }
            
            displayText = raw.name + " - " + displayValue + " " + displayUnit;
        } else {
            displayText = "Unknown Material - " + mapping.quantityRequired;
        }

        nameText.setText(displayText);

        deleteIcon.setOnClickListener(v -> {
            selectedMaterials.remove(mapping);
            refreshMaterialList();
        });

        editIcon.setOnClickListener(v -> {
            showMaterialSelector(mapping);
        });

        materialContainer.addView(view);
    }

    private void saveProduct(){
        String name = nameInput.getText().toString();
        String priceStr = priceInput.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please enter name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product();
        product.name = name;
        product.price = Double.parseDouble(priceStr);
        product.category = category;
        product.currentStock = 0; // Starting with 0 stock
        product.lowStockThreshold = 5;
        product.hasRecipe = !selectedMaterials.isEmpty();
        product.imagePath = currentImagePath;

        if(productId == -1){
            viewModel.insertProduct(product, selectedMaterials);
        } else {
            product.id = productId;
            viewModel.updateProduct(product, selectedMaterials);
        }
        finish();
    }
}