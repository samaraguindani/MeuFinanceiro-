package com.example.meufinanceiro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meufinanceiro.utils.GraficoBarrasHelper;
import com.example.meufinanceiro.utils.GraficoPizzaHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class OverviewFragment extends Fragment {

    private static final String ARG_MONTH_NAME = "month_name";
    private String monthName;

    private TextView tvResumo, tvCategoriaDestaque;
    private PieChart pieChart;
    private BarChart barChart;

    private FirebaseFirestore firestore;

    public OverviewFragment() {}

    public static OverviewFragment newInstance(String monthName) {
        OverviewFragment fragment = new OverviewFragment();
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
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        tvResumo = view.findViewById(R.id.tvResumoMes);
        tvCategoriaDestaque = view.findViewById(R.id.tvCategoriaDestaque);
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);

        carregarDados();

        return view;
    }

    private void carregarDados() {
        firestore.collection("transactions")
                .document(monthName)
                .collection("items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        tvResumo.setText("Total Recebido: R$ 0,00\nTotal Gasto: R$ 0,00");
                        tvCategoriaDestaque.setText("Maior categoria de gasto: -");
                        pieChart.clear();
                        barChart.clear();
                        return;
                    }

                    double totalGanho = 0;
                    double totalGasto = 0;
                    Map<String, Double> categoriaGastos = new HashMap<>();
                    Map<String, Double> gastosPorDia = new TreeMap<>();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t == null) continue;

                        if ("Ganho".equalsIgnoreCase(t.getTipo())) {
                            totalGanho += t.getValor();
                        } else {
                            totalGasto += t.getValor();

                            // Somar por categoria
                            categoriaGastos.put(t.getCategoria(),
                                    categoriaGastos.getOrDefault(t.getCategoria(), 0.0) + t.getValor());

                            // Somar por dia (formato simplificado: "07" por exemplo)
                            String dia = extrairDia(t.getData());
                            gastosPorDia.put(dia, gastosPorDia.getOrDefault(dia, 0.0) + t.getValor());
                        }
                    }

                    tvResumo.setText(String.format(Locale.getDefault(),
                            "Total Recebido: R$ %.2f\nTotal Gasto: R$ %.2f", totalGanho, totalGasto));

                    // Categoria destaque
                    String maiorCategoria = "-";
                    double maiorValor = 0;
                    for (Map.Entry<String, Double> entry : categoriaGastos.entrySet()) {
                        if (entry.getValue() > maiorValor) {
                            maiorCategoria = entry.getKey();
                            maiorValor = entry.getValue();
                        }
                    }
                    tvCategoriaDestaque.setText("Maior categoria de gasto: " + maiorCategoria);

                    // Gráficos
                    GraficoPizzaHelper.configurarGrafico(pieChart, categoriaGastos);
                    GraficoBarrasHelper.configurarGrafico(barChart, gastosPorDia);
                });
    }

    private String extrairDia(String data) {
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = formatoEntrada.parse(data);
            SimpleDateFormat formatoDia = new SimpleDateFormat("dd", Locale.getDefault());
            return formatoDia.format(date);
        } catch (Exception e) {
            return "?";
        }
    }
}
