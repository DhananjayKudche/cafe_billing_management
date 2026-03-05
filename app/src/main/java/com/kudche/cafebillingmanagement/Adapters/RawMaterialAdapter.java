package com.kudche.cafebillingmanagement.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kudche.cafebillingmanagement.Models.RawMaterial;
import com.kudche.cafebillingmanagement.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RawMaterialAdapter extends RecyclerView.Adapter<RawMaterialAdapter.ViewHolder> {

    List<RawMaterial> list = new ArrayList<>();
    Consumer<RawMaterial> deleteListener;
    Consumer<RawMaterial> editListener;

    public RawMaterialAdapter(Consumer<RawMaterial> deleteListener,
                              Consumer<RawMaterial> editListener) {
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    public void setList(List<RawMaterial> data){
        list = data;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, stock;
        Button deleteBtn, editBtn;

        ViewHolder(View v){
            super(v);

            name = v.findViewById(R.id.materialName);
            stock = v.findViewById(R.id.materialStock);
            deleteBtn = v.findViewById(R.id.deleteBtn);
            editBtn = v.findViewById(R.id.editBtn);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder h,int pos){

        RawMaterial m = list.get(pos);

        h.name.setText(m.name);
        h.stock.setText("Stock: " + m.currentStock + " " + m.unit);

        h.deleteBtn.setOnClickListener(v -> deleteListener.accept(m));
        h.editBtn.setOnClickListener(v -> editListener.accept(m));
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup p,int v){
        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_raw_material,p,false);
        return new ViewHolder(view);
    }
}