package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "product_ingredients",
        foreignKeys = {
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = RawMaterial.class,
                        parentColumns = "id",
                        childColumns = "rawMaterialId",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class ProductIngredient {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int productId;

    public int rawMaterialId;

    // Recipe ratio
    public double quantityRequired;
}