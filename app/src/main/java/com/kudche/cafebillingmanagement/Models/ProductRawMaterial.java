package com.kudche.cafebillingmanagement.Models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


    @Entity(
            tableName = "product_raw_material",
            foreignKeys = {
                    @ForeignKey(entity = Product.class,
                            parentColumns = "id",
                            childColumns = "productId",
                            onDelete = ForeignKey.CASCADE),

                    @ForeignKey(entity = RawMaterial.class,
                            parentColumns = "id",
                            childColumns = "rawMaterialId",
                            onDelete = ForeignKey.CASCADE)
            }
    )
    public class ProductRawMaterial {

        @PrimaryKey(autoGenerate = true)
        public int id;

        public int productId;
        public int rawMaterialId;

        public double quantityRequired; // Stored in base unit (KG, LITER, PCS)
}
