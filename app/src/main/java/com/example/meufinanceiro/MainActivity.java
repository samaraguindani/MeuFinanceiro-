package com.example.meufinanceiro;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.example.meufinanceiro.utils.UsuarioFirebase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private ImageView avatar;
    private FloatingActionButton fabAdd;
    private RecyclerView recycler;

    private FirebaseFirestore firestore;
    private FirebaseUser usuario;

    @Override
    protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        avatar  = findViewById(R.id.avatar);
        fabAdd  = findViewById(R.id.fabAdd);
        recycler= findViewById(R.id.recycler_months);

        firestore = FirebaseFirestore.getInstance();
        usuario = FirebaseAuth.getInstance().getCurrentUser();

        verificarUsuarioLogado();
        carregarAvatar();

        recycler.setLayoutManager(new LinearLayoutManager(this));

        fabAdd.setOnClickListener(v -> abrirDialogoNovoMes());

        carregarMesesDoFirestore();

        avatar.setOnClickListener(v -> mostrarDialogoLogout());

        configurarBottomNavigation();
    }

    private void configurarBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_transactions) {
                startActivity(new Intent(MainActivity.this, MonthDetailActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void verificarUsuarioLogado() {
        if (usuario == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void carregarAvatar() {
        Uri u = UsuarioFirebase.getFotoUsuario();
        if (u != null) {
            Glide.with(this).load(u).circleCrop().placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(avatar);
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
        }
    }

    private void abrirDialogoNovoMes() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_novo_mes, null);

        Spinner spinnerMes = view.findViewById(R.id.spinnerMes);
        Spinner spinnerAno = view.findViewById(R.id.spinnerAno);
        EditText editDescricao = view.findViewById(R.id.editDescricao);

        String[] meses = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };
        spinnerMes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, meses));

        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        List<String> anos = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            anos.add(String.valueOf(anoAtual - i));
        }
        spinnerAno.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, anos));

        new AlertDialog.Builder(this)
                .setTitle("Adicionar Mês")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String mesSelecionado = spinnerMes.getSelectedItem().toString();
                    String anoSelecionado = spinnerAno.getSelectedItem().toString();
                    String descricao = editDescricao.getText().toString().trim();
                    String nomeMes = mesSelecionado + " " + anoSelecionado;
                    salvarMesNoFirestore(nomeMes, descricao);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void salvarMesNoFirestore(String nomeMes, String descricao) {
        if (usuario == null) {
            Toast.makeText(this, "Usuário não logado!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> dadosMes = new HashMap<>();
        dadosMes.put("name", nomeMes);
        dadosMes.put("descricao", descricao);
        dadosMes.put("totalBalance", 0.0);
        dadosMes.put("isCurrent", false);

        firestore.collection("users").document(usuario.getUid()).collection("months")
                .add(dadosMes)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Mês adicionado!", Toast.LENGTH_SHORT).show();
                    carregarMesesDoFirestore();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao adicionar mês.", Toast.LENGTH_SHORT).show());
    }

    private void carregarMesesDoFirestore() {
        if (usuario == null) return;

        firestore.collection("users").document(usuario.getUid()).collection("months")
                .get()
                .addOnSuccessListener(query -> {
                    List<MonthSummary> months = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        String name = doc.getString("name");
                        String descricao = doc.getString("descricao");
                        String nomeExibicao = name;
                        if (descricao != null && !descricao.isEmpty()) {
                            nomeExibicao += " (" + descricao + ")";
                        }
                        calcularSaldoDoMes(doc.getId(), nomeExibicao, months);
                    }
                });
    }

    private void calcularSaldoDoMes(String monthId, String nomeExibicao, List<MonthSummary> months) {
        firestore.collection("transactions").document(nomeExibicao).collection("items")
                .get()
                .addOnSuccessListener(transactions -> {
                    double saldo = 0.0;
                    for (var t : transactions.getDocuments()) {
                        double valor = t.getDouble("valor");
                        String tipo = t.getString("tipo");
                        if ("Ganho".equalsIgnoreCase(tipo)) {
                            saldo += valor;
                        } else if ("Gasto".equalsIgnoreCase(tipo)) {
                            saldo -= valor;
                        }
                    }
                    String saldoFormatado = (saldo >= 0 ? "+" : "-") + "R$ " + String.format("%.2f", Math.abs(saldo));
                    months.add(new MonthSummary(nomeExibicao, saldoFormatado, false));
                    recycler.setAdapter(new MonthAdapter(MainActivity.this, months));
                });
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

    static class MonthSummary {
        String name, balance;
        boolean isCurrent;
        MonthSummary(String name, String balance, boolean isCurrent) {
            this.name = name;
            this.balance = balance;
            this.isCurrent = isCurrent;
        }
    }

    // Nosso adapter já com exclusão:
    class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {
        private final List<MonthSummary> months;
        private final Context context;
        private final FirebaseFirestore firestore;
        private final FirebaseUser usuario;

        MonthAdapter(Context context, List<MonthSummary> months) {
            this.context = context;
            this.months = months;
            this.firestore = FirebaseFirestore.getInstance();
            this.usuario = FirebaseAuth.getInstance().getCurrentUser();
        }

        @NonNull @Override
        public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month, parent, false);
            return new MonthViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
            MonthSummary m = months.get(position);
            holder.tvCurrent.setVisibility(m.isCurrent ? View.VISIBLE : View.GONE);
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
                        for (var doc : query.getDocuments()) {
                            doc.getReference().delete();
                        }
                        firestore.collection("transactions").document(nomeMes).collection("items")
                                .get().addOnSuccessListener(items -> {
                                    for (var item : items.getDocuments()) {
                                        item.getReference().delete();
                                    }
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

        @Override public int getItemCount() { return months.size(); }

        class MonthViewHolder extends RecyclerView.ViewHolder {
            TextView tvCurrent, tvMonthName, tvBalance;
            MonthViewHolder(View itemView) {
                super(itemView);
                tvCurrent = itemView.findViewById(R.id.tvCurrent);
                tvMonthName = itemView.findViewById(R.id.tvMonthName);
                tvBalance = itemView.findViewById(R.id.tvBalance);
            }
        }
    }
}
