package com.example.meufinanceiro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.example.meufinanceiro.R;
import com.example.meufinanceiro.utils.UsuarioFirebase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private ImageView avatar;
    private FloatingActionButton fabAdd;
    private RecyclerView recycler;

    @Override
    protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        avatar  = findViewById(R.id.avatar);
        fabAdd  = findViewById(R.id.fabAdd);
        recycler= findViewById(R.id.recycler_months);

        // Verifica se o usuário está logado, senão volta pro login
        verificarUsuarioLogado();

        // carrega avatar
        carregarAvatar();

        // configura RecyclerView
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // prepara dados de exemplo
        List<MonthSummary> months = Arrays.asList(
                new MonthSummary("June 2025", "+R$ 1,500", true),
                new MonthSummary("May 2025",  "+R$ 1,200", false),
                new MonthSummary("April 2025", "+R$ 1,800", false)
        );

        // seta adapter
        recycler.setAdapter(new MonthAdapter(months));

        fabAdd.setOnClickListener(v ->
                Toast.makeText(this, "Adicionar item", Toast.LENGTH_SHORT).show()
        );

        // clique no avatar para logout
        avatar.setOnClickListener(v -> mostrarDialogoLogout());
    }

    private void verificarUsuarioLogado() {
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioAtual == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void carregarAvatar() {
        Uri u = UsuarioFirebase.getFotoUsuario();
        if (u != null) {
            Glide.with(this)
                    .load(u)
                    .circleCrop()
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(avatar);
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
        }
    }

    private void mostrarDialogoLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair?")
                .setPositiveButton("Sair", (dialog, which) -> deslogarUsuario())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deslogarUsuario() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ——————— Data class ———————
    static class MonthSummary {
        String name, balance;
        boolean isCurrent;
        MonthSummary(String name, String balance, boolean isCurrent) {
            this.name = name;
            this.balance = balance;
            this.isCurrent = isCurrent;
        }
    }

    // ——————— Adapter ———————
    class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {
        private final List<MonthSummary> months;
        MonthAdapter(List<MonthSummary> months) { this.months = months; }

        @NonNull @Override
        public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_month, parent, false);
            return new MonthViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MonthViewHolder h, int pos) {
            MonthSummary m = months.get(pos);
            h.tvCurrent.setVisibility(m.isCurrent ? View.VISIBLE : View.GONE);
            h.tvMonthName.setText(m.name);
            h.tvBalance.setText(m.balance);

            h.itemView.setOnClickListener(v -> {
                Intent it = new Intent(v.getContext(), MonthDetailActivity.class);
                it.putExtra("EXTRA_MONTH_NAME", m.name);
                v.getContext().startActivity(it);
            });
        }

        @Override public int getItemCount() { return months.size(); }

        class MonthViewHolder extends RecyclerView.ViewHolder {
            TextView tvCurrent, tvMonthName, tvBalance;
            MonthViewHolder(View itemView) {
                super(itemView);
                tvCurrent   = itemView.findViewById(R.id.tvCurrent);
                tvMonthName = itemView.findViewById(R.id.tvMonthName);
                tvBalance   = itemView.findViewById(R.id.tvBalance);
            }
        }
    }
}
