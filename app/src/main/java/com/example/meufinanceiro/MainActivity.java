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
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.example.meufinanceiro.R;
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
                // já está na Home
                return true;
            }
            else if (id == R.id.nav_settings) {
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

    private void abrirDialogoNovoMes() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_novo_mes, null);
        EditText editNomeMes = view.findViewById(R.id.editNomeMes);

        new AlertDialog.Builder(this)
                .setTitle("Adicionar Mês")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nomeMes = editNomeMes.getText().toString().trim();
                    if (!nomeMes.isEmpty()) {
                        salvarMesNoFirestore(nomeMes);
                    } else {
                        Toast.makeText(this, "Digite o nome do mês", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void salvarMesNoFirestore(String nomeMes) {
        if (usuario == null) {
            Toast.makeText(this, "Usuário não logado!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> dadosMes = new HashMap<>();
        dadosMes.put("name", nomeMes);
        dadosMes.put("totalBalance", 0.0);
        dadosMes.put("isCurrent", false);

        firestore.collection("users")
                .document(usuario.getUid())
                .collection("months")
                .add(dadosMes)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Mês adicionado!", Toast.LENGTH_SHORT).show();
                    carregarMesesDoFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao adicionar mês.", Toast.LENGTH_SHORT).show();
                });
    }

    private void carregarMesesDoFirestore() {
        if (usuario == null) return;

        firestore.collection("users")
                .document(usuario.getUid())
                .collection("months")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MonthSummary> months = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString("name");
                        Double balance = doc.getDouble("totalBalance");
                        Boolean isCurrent = doc.getBoolean("isCurrent");
                        months.add(new MonthSummary(name, "+R$ " + (balance != null ? balance.intValue() : 0), isCurrent != null && isCurrent));
                    }
                    recycler.setAdapter(new MonthAdapter(months));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar meses", Toast.LENGTH_SHORT).show();
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
