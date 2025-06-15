package com.example.meufinanceiro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meufinanceiro.R;
import com.example.meufinanceiro.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> lista;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> lista, OnItemClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = lista.get(position);
        holder.txtValor.setText(String.format("R$ %.2f", t.getValor()));
        holder.txtData.setText(t.getData());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(t));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtValor, txtData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtValor = itemView.findViewById(R.id.txtValor);
            txtData = itemView.findViewById(R.id.txtData);
        }
    }
}
