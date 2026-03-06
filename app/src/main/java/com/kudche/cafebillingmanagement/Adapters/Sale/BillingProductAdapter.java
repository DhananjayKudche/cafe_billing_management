package com.kudche.cafebillingmanagement.Adapters.Sale;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BillingProductAdapter extends RecyclerView.Adapter<BillingProductAdapter.ViewHolder> {

    public interface ProductClickListener{
        void onProductClick(Product product);
    }

    private List<Product> products = new ArrayList<>();
    private ProductClickListener listener;

    public BillingProductAdapter(ProductClickListener listener){
        this.listener = listener;
    }

    public void setProducts(List<Product> products){
        this.products = products;
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

        if (product.imagePath != null && !product.imagePath.isEmpty()) {
            if (product.imagePath.startsWith("ic_")) {
                // Load from drawable resources
                int resId = context.getResources().getIdentifier(product.imagePath, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.productImage.setImageResource(resId);
                } else {
                    holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                // Load from file path
                holder.productImage.setImageURI(Uri.fromFile(new File(product.imagePath)));
            }
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView productName;
        TextView productPrice;
        ImageView productImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);
        }
    }
}
