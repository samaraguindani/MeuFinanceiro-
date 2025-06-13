package com.example.meufinanceiro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoryPaymentAdapter extends RecyclerView.Adapter<CategoryPaymentAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CategoryPaymentItem item);
    }

    private ArrayList<CategoryPaymentItem> data = new ArrayList<>();
    private OnItemClickListener listener;

    public void setData(ArrayList<CategoryPaymentItem> lista) {
        data = lista;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryPaymentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryPaymentAdapter.ViewHolder holder, int position) {
        CategoryPaymentItem item = data.get(position);
        holder.txt.setText(item.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt;
        ViewHolder(View itemView) {
            super(itemView);
            txt = itemView.findViewById(android.R.id.text1);
        }
    }
}
