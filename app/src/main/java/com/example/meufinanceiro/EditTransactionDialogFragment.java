package com.example.meufinanceiro;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.meufinanceiro.R;
import com.example.meufinanceiro.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class EditTransactionDialogFragment extends DialogFragment {

    private String mesSelecionado;
    private Transaction transaction;

    private EditText etValor, etDescricao, etData;
    private Spinner spinnerCategoria, spinnerFormaPagamento;
    private RadioGroup radioTipo;

    private FirebaseFirestore firestore;
    private String userId;

    private final Calendar calendar = Calendar.getInstance();

    public EditTransactionDialogFragment(String mesSelecionado, Transaction transaction) {
        this.mesSelecionado = mesSelecionado;
        this.transaction = transaction;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.add_transaction_dialog, null);

        etValor = view.findViewById(R.id.etValor);
        etDescricao = view.findViewById(R.id.etDescricao);
        etData = view.findViewById(R.id.etData);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        spinnerFormaPagamento = view.findViewById(R.id.spinnerFormaPagamento);
        radioTipo = view.findViewById(R.id.radioTipo);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        carregarCategorias();
        carregarPagamentos();

        preencherCampos();

        etData.setOnClickListener(v -> abrirDatePicker());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Editar Transação");
        builder.setView(view);

        builder.setPositiveButton("Atualizar", (dialog, which) -> atualizarTransacao());
        builder.setNeutralButton("Excluir", (dialog, which) -> excluirTransacao());
        builder.setNegativeButton("Cancelar", null);

        return builder.create();
    }

    private void preencherCampos() {
        etValor.setText(String.valueOf(transaction.getValor()));
        etDescricao.setText(transaction.getDescricao());
        etData.setText(transaction.getData());

        if ("Ganho".equals(transaction.getTipo())) {
            radioTipo.check(R.id.rbGanho);
        } else {
            radioTipo.check(R.id.rbGasto);
        }
    }

    private void abrirDatePicker() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(etData.getText().toString());
            calendar.setTime(date);
        } catch (Exception e) {}

        int ano = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etData.setText(sdf.format(calendar.getTime()));
        }, ano, mes, dia).show();
    }

    private void carregarCategorias() {
        firestore.collection("users").document(userId).collection("categories")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> categorias = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        categorias.add(doc.getString("name"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categorias);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategoria.setAdapter(adapter);

                    int pos = categorias.indexOf(transaction.getCategoria());
                    if (pos >= 0) spinnerCategoria.setSelection(pos);
                });
    }

    private void carregarPagamentos() {
        firestore.collection("users").document(userId).collection("paymentMethods")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> pagamentos = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        pagamentos.add(doc.getString("name"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, pagamentos);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerFormaPagamento.setAdapter(adapter);

                    int pos = pagamentos.indexOf(transaction.getFormaPagamento());
                    if (pos >= 0) spinnerFormaPagamento.setSelection(pos);
                });
    }

    private void atualizarTransacao() {
        try {
            double valor = Double.parseDouble(etValor.getText().toString());
            String descricao = etDescricao.getText().toString();
            String data = etData.getText().toString();
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String formaPagamento = spinnerFormaPagamento.getSelectedItem().toString();
            String tipo = (radioTipo.getCheckedRadioButtonId() == R.id.rbGanho) ? "Ganho" : "Gasto";

            Map<String, Object> atualizacao = new HashMap<>();
            atualizacao.put("valor", valor);
            atualizacao.put("descricao", descricao);
            atualizacao.put("data", data);
            atualizacao.put("categoria", categoria);
            atualizacao.put("formaPagamento", formaPagamento);
            atualizacao.put("tipo", tipo);

            firestore.collection("transactions")
                    .document(mesSelecionado)
                    .collection("items")
                    .document(transaction.getId())
                    .update(atualizacao);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Preencha corretamente os campos!", Toast.LENGTH_SHORT).show();
        }
    }

    private void excluirTransacao() {
        firestore.collection("transactions")
                .document(mesSelecionado)
                .collection("items")
                .document(transaction.getId())
                .delete();
    }
}
