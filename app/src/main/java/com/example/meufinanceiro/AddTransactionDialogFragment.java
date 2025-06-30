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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddTransactionDialogFragment extends DialogFragment {

    private String mesSelecionado;
    private Spinner spinnerCategoria, spinnerFormaPagamento;
    private EditText etValor, etDescricao, etData;
    private RadioGroup radioTipo;

    private FirebaseFirestore firestore;
    private String userId;

    private final Calendar calendar = Calendar.getInstance();

    public AddTransactionDialogFragment(String mesSelecionado) {
        this.mesSelecionado = mesSelecionado;
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

        // Define data atual como padrão
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etData.setText(sdf.format(calendar.getTime()));

        etData.setOnClickListener(v -> abrirDatePicker());

        return new AlertDialog.Builder(requireContext())
                .setTitle("Adicionar Transação")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> salvarTransacao())
                .setNegativeButton("Cancelar", null)
                .create();
    }

    private void abrirDatePicker() {
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
                });
    }

    private void salvarTransacao() {
        try {
            double valor = Double.parseDouble(etValor.getText().toString());
            String descricao = etDescricao.getText().toString();
            String data = etData.getText().toString();
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String formaPagamento = spinnerFormaPagamento.getSelectedItem().toString();
            String tipo = (radioTipo.getCheckedRadioButtonId() == R.id.rbGanho) ? "Ganho" : "Gasto";

            // ------------ VALIDAÇÃO DO MÊS ---------------
            // Pega o nome do mês da tela, desconsiderando descrição:
            String nomeMesTela = mesSelecionado.split(" \\(")[0];  // Ex: Maio 2025

            // Extrai o mês e ano da data digitada:
            String[] partesData = data.split("/");

            if (partesData.length != 3) {
                Toast.makeText(getContext(), "Data inválida!", Toast.LENGTH_SHORT).show();
                return;
            }

            String mesInformado = partesData[1];  // Ex: 05
            String anoInformado = partesData[2];  // Ex: 2025

            String[] nomesMeses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

            int mesIndex = Integer.parseInt(mesInformado) - 1;
            String mesConvertido = nomesMeses[mesIndex] + " " + anoInformado;

            if (!nomeMesTela.equals(mesConvertido)) {
                Toast.makeText(getContext(), "A data não pertence ao mês selecionado.", Toast.LENGTH_SHORT).show();
                return;
            }
            // ------------------------------------------------

            String id = firestore.collection("transactions").document(mesSelecionado).collection("items").document().getId();

            Transaction transaction = new Transaction(id, descricao, valor, data, categoria, formaPagamento, tipo);
            transaction.setUsuarioId(userId);

            firestore.collection("transactions")
                    .document(mesSelecionado)
                    .collection("items")
                    .document(id)
                    .set(transaction);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Preencha corretamente os campos!", Toast.LENGTH_SHORT).show();
        }
    }
}
