package com.example.konwerter3;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.konwerter3.databinding.FragmentChartBinding;
import com.example.konwerter3.model.Currency;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ChartFragment extends Fragment {

    private FragmentChartBinding binding;
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupChart();

        sharedViewModel.getSelectedCurrency().observe(getViewLifecycleOwner(), currency -> {
            if (currency != null) {
                updateChart(currency);
            } else {
                binding.lineChart.clear();
                binding.lineChart.invalidate();
            }
        });
    }

    private void setupChart() {
        binding.lineChart.getDescription().setEnabled(false);
        binding.lineChart.setNoDataText("Wybierz walutę, aby zobaczyć wykres");
        binding.lineChart.setNoDataTextColor(Color.BLACK);
    }

    private void updateChart(Currency currency) {
        List<Entry> entries = generateDummyData(currency.getExchangeRate());

        LineDataSet dataSet = new LineDataSet(entries, "Kurs z ostatnich 7 dni");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setDrawCircleHole(false);

        LineData lineData = new LineData(dataSet);
        binding.lineChart.setData(lineData);

        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getPastDates(7)));

        binding.lineChart.invalidate();
    }

    private List<Entry> generateDummyData(double currentRate) {
        List<Entry> entries = new ArrayList<>();
        Random random = new Random();
        double rate = currentRate;

        for (int i = 6; i > 0; i--) {
            entries.add(new Entry(6 - i, (float) rate));
            rate = rate * (1 + (random.nextDouble() - 0.5) / 10);
        }
        entries.add(new Entry(6, (float) currentRate));

        return entries;
    }

    private List<String> getPastDates(int days) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < days; i++) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            dates.add(sdf.format(cal.getTime()));
        }
        Collections.reverse(dates);
        return dates;
    }
}
