package com.kudche.cafebillingmanagement.Adapters.Sale;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingProductAdapter extends RecyclerView.Adapter<BillingProductAdapter.ViewHolder> {

    public interface ProductClickListener {
        void onProductClick(Product product);
        void onQuantityChange(Product product, int newQuantity);
    }

    private List<Product> products = new ArrayList<>();
    private ProductClickListener listener;
    private Map<Integer, Integer> productQuantities = new HashMap<>();

    public BillingProductAdapter(ProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public void setQuantities(Map<Integer, Integer> quantities) {
        this.productQuantities = quantities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        Context context = holder.itemView.getContext();

        holder.productName.setText(product.name);
        holder.productPrice.setText("₹" + product.price);

        final int quantity = productQuantities.containsKey(product.id) ? productQuantities.get(product.id) : 0;
        holder.tvQuantity.setText(String.valueOf(quantity));

        if (product.imagePath != null && !product.imagePath.isEmpty()) {
            if (product.imagePath.startsWith("ic_")) {
                int resId = context.getResources().getIdentifier(product.imagePath, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.productImage.setImageResource(resId);
                } else {
                    holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.productImage.setImageURI(Uri.fromFile(new File(product.imagePath)));
            }
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnPlus.setOnClickListener(v -> {
            int currentQty = productQuantities.containsKey(product.id) ? productQuantities.get(product.id) : 0;
            int newQty = currentQty + 1;
            productQuantities.put(product.id, newQty);
            holder.tvQuantity.setText(String.valueOf(newQty));
            if (listener != null) {
                listener.onQuantityChange(product, newQty);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            int currentQty = productQuantities.containsKey(product.id) ? productQuantities.get(product.id) : 0;
            if (currentQty > 0) {
                int newQty = currentQty - 1;
                productQuantities.put(product.id, newQty);
                holder.tvQuantity.setText(String.valueOf(newQty));
                if (listener != null) {
                    listener.onQuantityChange(product, newQty);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, tvQuantity;
        ImageView productImage;
        Button btnMinus, btnPlus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
