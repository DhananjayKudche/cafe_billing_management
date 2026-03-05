package com.kudche.cafebillingmanagement.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.R;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter
        extends RecyclerView.Adapter<ProductAdapter.ViewHolder>{

    private List<Product> list = new ArrayList<>();

    private final OnEditClick editClick;
    private final OnDeleteClick deleteClick;

    public interface OnEditClick{
        void onEdit(Product product);
    }

    public interface OnDeleteClick{
        void onDelete(Product product);
    }

    public ProductAdapter(OnEditClick editClick,
                          OnDeleteClick deleteClick){
        this.editClick = editClick;
        this.deleteClick = deleteClick;
    }

    public void setList(List<Product> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_manage, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Product product = list.get(position);

        holder.name.setText(product.name);
        holder.price.setText("₹ " + product.price);

        holder.edit.setOnClickListener(v ->
                editClick.onEdit(product));

        holder.delete.setOnClickListener(v ->
                deleteClick.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, price;
        Button edit, delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            edit = itemView.findViewById(R.id.editBtn);
            delete = itemView.findViewById(R.id.deleteBtn);
        }
    }
}