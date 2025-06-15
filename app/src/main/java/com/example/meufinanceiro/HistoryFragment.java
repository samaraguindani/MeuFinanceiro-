package com.example.meufinanceiro.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.meufinanceiro.R;
import com.example.meufinanceiro.TransactionAdapter;
import com.example.meufinanceiro.Transaction;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private static final String ARG_MONTH_NAME = "month_name";
    private String monthName;

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();

    public HistoryFragment() {
        // Required empty public constructor
    }

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(adapter);

        carregarTransacoes();

        return view;
    }

    private void carregarTransacoes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("transactions")
                .document(monthName)
                .collection("items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    transactionList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        transactionList.add(transaction);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
