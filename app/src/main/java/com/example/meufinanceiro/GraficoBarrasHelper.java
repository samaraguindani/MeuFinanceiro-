package com.example.meufinanceiro.utils;

import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraficoBarrasHelper {

    public static void configurarGrafico(BarChart barChart, Map<String, Double> gastosPorDia) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> dias = new ArrayList<>(gastosPorDia.keySet());

        for (int i = 0; i < dias.size(); i++) {
            entries.add(new BarEntry(i, gastosPorDia.get(dias.get(i)).floatValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Gastos por Dia");
        dataSet.setColors(new int[]{
                Color.rgb(156, 39, 176),
                Color.rgb(171, 71, 188),
                Color.rgb(186, 104, 200)
        });
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
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
