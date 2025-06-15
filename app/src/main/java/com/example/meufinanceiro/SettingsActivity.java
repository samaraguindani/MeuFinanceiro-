package com.example.meufinanceiro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private String userId;

    private RecyclerView recyclerCategories, recyclerPayments;
    private CategoryPaymentAdapter categoryAdapter, paymentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerCategories = findViewById(R.id.recyclerCategories);
        recyclerPayments = findViewById(R.id.recyclerPayments);

        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerPayments.setLayoutManager(new LinearLayoutManager(this));

        categoryAdapter = new CategoryPaymentAdapter();
        paymentAdapter = new CategoryPaymentAdapter();

        recyclerCategories.setAdapter(categoryAdapter);
        recyclerPayments.setAdapter(paymentAdapter);

        findViewById(R.id.btnAddCategory).setOnClickListener(v -> abrirDialogCategoria());
        findViewById(R.id.btnAddPaymentMethod).setOnClickListener(v -> abrirDialogPagamento());

        categoryAdapter.setOnItemClickListener(this::editarExcluirCategoria);
        paymentAdapter.setOnItemClickListener(this::editarExcluirFormaPagamento);

        carregarCategorias();
        carregarFormasPagamento();
        configurarBottomNavigation();
    }

    // Adicionar nova categoria
    private void abrirDialogCategoria() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText editName = view.findViewById(R.id.editCategoryName);

        new AlertDialog.Builder(this)
                .setTitle("Nova Categoria")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Preencha o nome", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    salvarCategoria(name);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void salvarCategoria(String name) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("name", name);

        firestore.collection("users").document(userId)
                .collection("categories")
                .add(dados)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Categoria salva!", Toast.LENGTH_SHORT).show();
                    carregarCategorias();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show());
    }

    // Adicionar nova forma de pagamento
    private void abrirDialogPagamento() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_payment_method, null);
        EditText editName = view.findViewById(R.id.editPaymentMethodName);

        new AlertDialog.Builder(this)
                .setTitle("Nova Forma de Pagamento")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Preencha o nome", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    salvarFormaPagamento(name);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void salvarFormaPagamento(String name) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("name", name);

        firestore.collection("users").document(userId)
                .collection("paymentMethods")
                .add(dados)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Forma de pagamento salva!", Toast.LENGTH_SHORT).show();
                    carregarFormasPagamento();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show());
    }

    // Listar categorias
    private void carregarCategorias() {
        firestore.collection("users").document(userId).collection("categories")
                .get()
                .addOnSuccessListener(query -> {
                    ArrayList<CategoryPaymentItem> lista = new ArrayList<>();
                    query.getDocuments().forEach(doc -> {
                        lista.add(new CategoryPaymentItem(doc.getId(), doc.getString("name")));
                    });
                    categoryAdapter.setData(lista);
                });
    }

    // Listar formas de pagamento
    private void carregarFormasPagamento() {
        firestore.collection("users").document(userId).collection("paymentMethods")
                .get()
                .addOnSuccessListener(query -> {
                    ArrayList<CategoryPaymentItem> lista = new ArrayList<>();
                    query.getDocuments().forEach(doc -> {
                        lista.add(new CategoryPaymentItem(doc.getId(), doc.getString("name")));
                    });
                    paymentAdapter.setData(lista);
                });
    }

    // Edição / Exclusão de categoria
    private void editarExcluirCategoria(CategoryPaymentItem item) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText editName = view.findViewById(R.id.editCategoryName);
        editName.setText(item.getName());

        new AlertDialog.Builder(this)
                .setTitle("Editar Categoria")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoNome = editName.getText().toString().trim();
                    if (!novoNome.isEmpty()) {
                        atualizarCategoria(item.getId(), novoNome);
                    }
                })
                .setNeutralButton("Excluir", (dialog, which) -> excluirCategoria(item.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarCategoria(String id, String novoNome) {
        firestore.collection("users").document(userId)
                .collection("categories")
                .document(id)
                .update("name", novoNome)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Categoria atualizada!", Toast.LENGTH_SHORT).show();
                    carregarCategorias();
                });
    }

    private void excluirCategoria(String id) {
        firestore.collection("users").document(userId)
                .collection("categories")
                .document(id)
                .delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Categoria excluída!", Toast.LENGTH_SHORT).show();
                    carregarCategorias();
                });
    }

    // Edição / Exclusão de forma de pagamento
    private void editarExcluirFormaPagamento(CategoryPaymentItem item) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_payment_method, null);
        EditText editName = view.findViewById(R.id.editPaymentMethodName);
        editName.setText(item.getName());

        new AlertDialog.Builder(this)
                .setTitle("Editar Forma de Pagamento")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoNome = editName.getText().toString().trim();
                    if (!novoNome.isEmpty()) {
                        atualizarFormaPagamento(item.getId(), novoNome);
                    }
                })
                .setNeutralButton("Excluir", (dialog, which) -> excluirFormaPagamento(item.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarFormaPagamento(String id, String novoNome) {
        firestore.collection("users").document(userId)
                .collection("paymentMethods")
                .document(id)
                .update("name", novoNome)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Forma de pagamento atualizada!", Toast.LENGTH_SHORT).show();
                    carregarFormasPagamento();
                });
    }

    private void excluirFormaPagamento(String id) {
        firestore.collection("users").document(userId)
                .collection("paymentMethods")
                .document(id)
                .delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Forma de pagamento excluída!", Toast.LENGTH_SHORT).show();
                    carregarFormasPagamento();
                });
    }

    private void configurarBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_settings) {
                // já está na tela atual
                return true;
            }
            else if (id == R.id.nav_home) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                finish(); // fecha para não empilhar várias activities
                return true;
            }
            else if (id == R.id.nav_transactions) {
                carregarUltimoMesECriarIntent();
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

                        Intent intent = new Intent(SettingsActivity.this, MonthDetailActivity.class);
                        intent.putExtra("EXTRA_MONTH_NAME", nomeUltimoMes);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SettingsActivity.this, "Nenhum mês cadastrado ainda.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Erro ao buscar meses.", Toast.LENGTH_SHORT).show());
    }
}
