package com.example.meufinanceiro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meufinanceiro.utils.UsuarioFirebase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd;
    private RecyclerView recycler;
    private FirebaseFirestore firestore;
    private FirebaseUser usuario;

    @Override
    protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        firestore = FirebaseFirestore.getInstance();
        usuario = FirebaseAuth.getInstance().getCurrentUser();

        verificarUsuarioLogado();
        carregarAvatar();

        recycler = findViewById(R.id.recycler_months);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> abrirDialogoNovoMes());

        carregarMesesDoFirestore();

        configurarBottomNavigation();
    }

    private void configurarBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_transactions) {
                    carregarUltimoMesECriarIntent();
                    return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void carregarUltimoMesECriarIntent() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) return;

        firestore.collection("users")
                .document(usuario.getUid())
                .collection("months")
                .orderBy("name", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String nomeUltimoMes = queryDocumentSnapshots.getDocuments().get(0).getString("name");

                        Intent intent = new Intent(MainActivity.this, MonthDetailActivity.class);
                        intent.putExtra("EXTRA_MONTH_NAME", nomeUltimoMes);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Nenhum mês cadastrado ainda.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Erro ao buscar meses.", Toast.LENGTH_SHORT).show());
    }

    private void verificarUsuarioLogado() {
        if (usuario == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void carregarAvatar() {
        Uri u = UsuarioFirebase.getFotoUsuario();
        ImageView avatarImage = findViewById(R.id.avatar);
        if (u != null) {
            Glide.with(this)
                    .load(u)
                    .circleCrop()
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(avatarImage);
        } else {
        avatarImage.setImageResource(R.drawable.default_avatar);
        }
    }

    private void abrirDialogoNovoMes() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_novo_mes, null);

        Spinner spinnerMes = view.findViewById(R.id.spinnerMes);
        Spinner spinnerAno = view.findViewById(R.id.spinnerAno);
        EditText editDescricao = view.findViewById(R.id.editDescricao);

        String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        spinnerMes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, meses));

        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        List<String> anos = new ArrayList<>();
        for (int i = 0; i <= 10; i++) anos.add(String.valueOf(anoAtual - i));
        spinnerAno.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, anos));

        new AlertDialog.Builder(this)
                .setTitle("Adicionar Mês")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nomeMes = spinnerMes.getSelectedItem() + " " + spinnerAno.getSelectedItem();
                    String descricao = editDescricao.getText().toString().trim();
                    salvarMesNoFirestore(nomeMes, descricao);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void salvarMesNoFirestore(String nomeMes, String descricao) {
        if (usuario == null) return;

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
                });
    }

    public void carregarMesesDoFirestore() {
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
                        if ("Ganho".equalsIgnoreCase(tipo)) saldo += valor;
                        else if ("Gasto".equalsIgnoreCase(tipo)) saldo -= valor;
                    }
                    String saldoFormatado = (saldo >= 0 ? "+" : "-") + "R$ " + String.format("%.2f", Math.abs(saldo));
                    months.add(new MonthSummary(nomeExibicao, saldoFormatado, false));
                    recycler.setAdapter(new MonthAdapter(MainActivity.this, months));
                });
    }

    public static class MonthSummary {
        String name, balance;
        boolean isCurrent;
        MonthSummary(String name, String balance, boolean isCurrent) {
            this.name = name;
            this.balance = balance;
            this.isCurrent = isCurrent;
        }
    }
}
