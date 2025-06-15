package com.example.meufinanceiro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> lista;

    public TransactionAdapter(List<Transaction> lista) {
        this.lista = lista;
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
        holder.txtDescricao.setText(t.getDescricao());
        holder.txtValor.setText(String.format("R$ %.2f", t.getValor()));
        holder.txtCategoria.setText(t.getCategoria());
        holder.txtTipo.setText(t.getTipo());
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDescricao, txtValor, txtCategoria, txtTipo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtValor = itemView.findViewById(R.id.txtValor);
            txtCategoria = itemView.findViewById(R.id.txtCategoria);
            txtTipo = itemView.findViewById(R.id.txtTipo);
        }
    }
}
