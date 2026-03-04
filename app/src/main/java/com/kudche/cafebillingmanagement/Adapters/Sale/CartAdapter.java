package com.kudche.cafebillingmanagement.Adapters.Sale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Models.CartItem;
import com.kudche.cafebillingmanagement.R;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder>{

    private List<CartItem> cartItems = new ArrayList<>();
    public interface CartActionListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
    }
    private CartActionListener listener;

    public CartAdapter(CartActionListener listener){
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> items){
        this.cartItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CartItem item = cartItems.get(position);

        holder.name.setText(item.product.name);
        holder.qty.setText(String.valueOf(item.quantity));
        holder.price.setText("₹" + item.getTotalPrice());

        holder.plus.setOnClickListener(v -> listener.onIncrease(item));

        holder.minus.setOnClickListener(v -> listener.onDecrease(item));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,qty,price;
        Button plus,minus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.cartName);
            qty = itemView.findViewById(R.id.cartQty);
            price = itemView.findViewById(R.id.cartPrice);

            plus = itemView.findViewById(R.id.btnPlus);
            minus = itemView.findViewById(R.id.btnMinus);
        }
    }
}