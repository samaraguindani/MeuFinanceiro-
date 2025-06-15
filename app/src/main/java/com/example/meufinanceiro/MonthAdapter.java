package com.example.meufinanceiro;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {

    private final List<MainActivity.MonthSummary> months;
    private final Context context;
    private final FirebaseFirestore firestore;
    private final FirebaseUser usuario;

    public MonthAdapter(Context context, List<MainActivity.MonthSummary> months) {
        this.context = context;
        this.months = months;
        this.firestore = FirebaseFirestore.getInstance();
        this.usuario = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        MainActivity.MonthSummary m = months.get(position);
        holder.tvMonthName.setText(m.name);
        holder.tvBalance.setText(m.balance);

        holder.itemView.setOnClickListener(v -> {
            Intent it = new Intent(context, MonthDetailActivity.class);
            it.putExtra("EXTRA_MONTH_NAME", m.name);
            context.startActivity(it);
        });

        holder.itemView.setOnLongClickListener(v -> {
            mostrarDialogoExcluirMes(m.name);
            return true;
        });
    }

    private void mostrarDialogoExcluirMes(String nomeMes) {
        new AlertDialog.Builder(context)
                .setTitle("Excluir Mês")
                .setMessage("Deseja realmente excluir \"" + nomeMes + "\" e todas as suas transações?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirMesComTransacoes(nomeMes))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirMesComTransacoes(String nomeMes) {
        if (usuario == null) return;

        firestore.collection("users").document(usuario.getUid()).collection("months")
                .whereEqualTo("name", nomeMes).get().addOnSuccessListener(query -> {
                    for (var doc : query.getDocuments()) doc.getReference().delete();

                    firestore.collection("transactions").document(nomeMes).collection("items")
                            .get().addOnSuccessListener(items -> {
                                for (var item : items.getDocuments()) item.getReference().delete();

                                firestore.collection("transactions").document(nomeMes)
                                        .delete().addOnSuccessListener(aVoid -> {
                                            Toast.makeText(context, "Mês excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                            if (context instanceof MainActivity) {
                                                ((MainActivity) context).carregarMesesDoFirestore();
                                            }
                                        });
                            });
                }).addOnFailureListener(e -> Toast.makeText(context, "Erro ao excluir.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    static class MonthViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthName, tvBalance;
        MonthViewHolder(View itemView) {
            super(itemView);
            tvMonthName = itemView.findViewById(R.id.tvMonthName);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}
