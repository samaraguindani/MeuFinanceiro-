package com.example.meufinanceiro;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meufinanceiro.utils.UsuarioFirebase;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class OverviewFragment extends Fragment {

    private TextView tvGanhos, tvGastos, tvMaiorCategoria;
    private PieChart pieChart;
    private BarChart barChart;
    private FirebaseFirestore db;

    private final Map<String, Double> categoriaGastos = new HashMap<>();
    private final Map<String, Double> gastosPorDia = new TreeMap<>();
    private double totalGanhos = 0.0;
    private double totalGastos = 0.0;

    private String monthName; // Ex: "Maio 2025 (oi)"

    public static OverviewFragment newInstance(String monthName) {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putString("monthName", monthName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        monthName = getArguments() != null ? getArguments().getString("monthName", "") : "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        tvGanhos = view.findViewById(R.id.tvGanhos);
        tvGastos = view.findViewById(R.id.tvGastos);
        tvMaiorCategoria = view.findViewById(R.id.tvMaiorCategoria);
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);

        db = FirebaseFirestore.getInstance();

        carregarDados();

        return view;
    }

    private void carregarDados() {
        String uid = UsuarioFirebase.getUidUsuario();
        if (monthName == null || monthName.isEmpty()) return;

        db.collection("transactions")
                .document(monthName)
                .collection("items")
                .whereEqualTo("usuarioId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    totalGanhos = 0;
                    totalGastos = 0;
                    categoriaGastos.clear();
                    gastosPorDia.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Transaction t = doc.toObject(Transaction.class);

                        if ("Ganho".equalsIgnoreCase(t.getTipo())) {
                            totalGanhos += t.getValor();
                        } else {
                            totalGastos += t.getValor();

                            // Categoria
                            String cat = t.getCategoria();
                            if (cat != null) {
                                categoriaGastos.put(cat,
                                        categoriaGastos.getOrDefault(cat, 0.0) + t.getValor());
                            }

                            // Dia
                            String data = t.getData(); // formato: "07/05/2025"
                            if (data != null && data.length() >= 2) {
                                String dia = data.substring(0, 2);
                                gastosPorDia.put(dia,
                                        gastosPorDia.getOrDefault(dia, 0.0) + t.getValor());
                            }
                        }
                    }

                    atualizarResumo();
                    carregarGraficoPizza();
                    carregarGraficoBarras();
                })
                .addOnFailureListener(e -> {
                    tvGanhos.setText("Erro ao carregar dados.");
                    tvGastos.setText("");
                });
    }

    private void atualizarResumo() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        tvGanhos.setText("Total Recebido: " + nf.format(totalGanhos));
        tvGastos.setText("Total Gasto: " + nf.format(totalGastos));

        if (!categoriaGastos.isEmpty()) {
            String maiorCategoria = Collections.max(categoriaGastos.entrySet(), Map.Entry.comparingByValue()).getKey();
            tvMaiorCategoria.setText("Maior categoria de gasto: " + maiorCategoria);
        } else {
            tvMaiorCategoria.setText("Maior categoria de gasto: -");
        }
    }

    private void carregarGraficoPizza() {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoriaGastos.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Gastos por Categoria");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // ou: new int[]{Color.rgb(...)}
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setCenterText("Gastos");
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pieChart.invalidate();
    }

    private void carregarGraficoBarras() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> dias = new ArrayList<>(gastosPorDia.keySet());
        for (int i = 0; i < dias.size(); i++) {
            String dia = dias.get(i);
            double valor = gastosPorDia.get(dia);
            entries.add(new BarEntry(i, (float) valor));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Gastos por Dia");
        dataSet.setColors(new int[]{
                Color.rgb(150, 123, 182),
                Color.rgb(173, 130, 200),
                Color.rgb(192, 145, 210),
                Color.rgb(210, 160, 230),
                Color.rgb(230, 180, 250)
        });
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < dias.size()) ? dias.get(i) : "";
            }
        });

        barChart.invalidate();
    }
}
