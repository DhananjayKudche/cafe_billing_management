package com.kudche.cafebillingmanagement.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.ProductRawMaterial;
import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;
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

    List<ProductRawMaterial> selectedMaterials = new ArrayList<>();
    List<RawMaterial> rawMaterials = new ArrayList<>();

    int productId = -1;
    String currentImagePath = null;

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

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        rawViewModel = new ViewModelProvider(this).get(RawMaterialViewModel.class);

        nameInput = findViewById(R.id.productName);
        priceInput = findViewById(R.id.productPrice);
        materialContainer = findViewById(R.id.materialContainer);
        addMaterialBtn = findViewById(R.id.addMaterialBtn);
        productImage = findViewById(R.id.productImage);
        selectImageBtn = findViewById(R.id.selectImageBtn);

        productId = getIntent().getIntExtra("productId",-1);

        if(productId != -1){
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
        Spinner spinner = view.findViewById(R.id.rawMaterialSpinner);
        EditText qtyInput = view.findViewById(R.id.quantityInput);
        TextView unitText = view.findViewById(R.id.unitText);

        ArrayAdapter<RawMaterial> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rawMaterials);
        spinner.setAdapter(adapter);

        if (existingMapping != null) {
            qtyInput.setText(String.valueOf(existingMapping.quantityRequired));
            for (int i = 0; i < rawMaterials.size(); i++) {
                if (rawMaterials.get(i).id == existingMapping.rawMaterialId) {
                    spinner.setSelection(i);
                    unitText.setText(rawMaterials.get(i).unit);
                    break;
                }
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                unitText.setText(rawMaterials.get(position).unit);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        new AlertDialog.Builder(this)
                .setTitle(existingMapping == null ? "Add Raw Material" : "Edit Raw Material")
                .setView(view)
                .setPositiveButton(existingMapping == null ? "Add" : "Update", (d, w) -> {
                    RawMaterial selected = (RawMaterial) spinner.getSelectedItem();
                    String qtyStr = qtyInput.getText().toString();
                    if(qtyStr.isEmpty()) return;

                    double qty = Double.parseDouble(qtyStr);

                    if (existingMapping == null) {
                        ProductRawMaterial mapping = new ProductRawMaterial();
                        mapping.rawMaterialId = selected.id;
                        mapping.quantityRequired = qty;
                        selectedMaterials.add(mapping);
                    } else {
                        existingMapping.rawMaterialId = selected.id;
                        existingMapping.quantityRequired = qty;
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

        String displayText = (raw != null) ? (raw.name + " - " + mapping.quantityRequired + " " + raw.unit)
                                           : ("Unknown Material - " + mapping.quantityRequired);
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

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please enter name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product();
        product.name = name;
        product.price = Double.parseDouble(priceStr);
        product.currentStock = 50;
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