package com.example.meufinanceiro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meufinanceiro.R;
import com.example.meufinanceiro.TransactionAdapter;
import com.example.meufinanceiro.EditTransactionDialogFragment;
import com.example.meufinanceiro.Transaction;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private static final String ARG_MONTH_NAME = "month_name";
    private String monthName;

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private FirebaseFirestore firestore;

    public HistoryFragment() {}

    public static HistoryFragment newInstance(String monthName) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MONTH_NAME, monthName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            monthName = getArguments().getString(ARG_MONTH_NAME);
        }
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TransactionAdapter(transactionList, this::editarTransacao);
        recyclerView.setAdapter(adapter);

        carregarTransacoes();

        return view;
    }

    private void carregarTransacoes() {
        firestore.collection("transactions")
                .document(monthName)
                .collection("items")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        transactionList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Transaction t = doc.toObject(Transaction.class);
                            transactionList.add(t);
                        }

                        // Ordenação manual após carregar
                        Collections.sort(transactionList, (t1, t2) -> {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date data1 = sdf.parse(t1.getData());
                                Date data2 = sdf.parse(t2.getData());
                                return data2.compareTo(data1); // ordem decrescente
                            } catch (Exception e) {
                                return 0; // fallback se não conseguir converter
                            }
                        });

                        adapter.notifyDataSetChanged();
                    }
                });
    }




    private void excluirTransacao(Transaction transaction) {
        firestore.collection("transactions")
                .document(monthName)
                .collection("items")
                .document(transaction.getId())
                .delete();
    }

    private void editarTransacao(Transaction transaction) {
        EditTransactionDialogFragment dialog = new EditTransactionDialogFragment(monthName, transaction);
        dialog.show(getParentFragmentManager(), "EditarTransacao");
    }
}
