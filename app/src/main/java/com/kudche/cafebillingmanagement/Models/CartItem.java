package com.kudche.cafebillingmanagement.Models;

public class CartItem {

    public Product product;

    public int quantity = 1;

    public double getTotalPrice(){
        return product.price * quantity;
    }
}