package com.kudche.cafebillingmanagement.Activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kudche.cafebillingmanagement.Database.AppDatabase;
import com.kudche.cafebillingmanagement.Models.Recipe;
import com.kudche.cafebillingmanagement.R;

public class RecipeActivity extends AppCompatActivity {

    Spinner productSpinner, materialSpinner;
    EditText qtyInput;

    Button saveBtn;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);


    }

//    private void loadProducts() {
//
//        new Thread(() -> {
//
//            var products = db.productDao().getAllSync();
//
//            runOnUiThread(() -> {
//
//                ArrayAdapter adapter = new ArrayAdapter(
//                        this,
//                        android.R.layout.simple_spinner_dropdown_item,
//                        products
//                );
//
//                productSpinner.setAdapter(adapter);
//            });
//
//        }).start();
//    }
//
//    private void loadMaterials() {
//
//        new Thread(() -> {
//
//            var materials = db.rawMaterialDao().getAllSync();
//
//            runOnUiThread(() -> {
//
//                ArrayAdapter adapter = new ArrayAdapter(
//                        this,
//                        android.R.layout.simple_spinner_dropdown_item,
//                        materials
//                );
//
//                materialSpinner.setAdapter(adapter);
//            });
//
//        }).start();
//    }
//
//    private void saveRecipe() {
//
//        new Thread(() -> {
//
//            Recipe recipe = new Recipe();
//
//            recipe.productId = ((com.kudche.cafebillingmanagement.Models.Product)
//                    productSpinner.getSelectedItem()).id;
//
//            recipe.rawMaterialId = ((com.kudche.cafebillingmanagement.Models.RawMaterial)
//                    materialSpinner.getSelectedItem()).id;
//
//            recipe.quantityRequired = Double.parseDouble(qtyInput.getText().toString());
//
//            db.recipeDao().insert(recipe);
//
//            runOnUiThread(() ->
//                    Toast.makeText(this,"Recipe saved",Toast.LENGTH_SHORT).show());
//
//        }).start();
//    }
}