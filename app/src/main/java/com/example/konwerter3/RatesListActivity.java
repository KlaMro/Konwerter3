package com.example.konwerter3;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konwerter3.model.Currency;

import java.util.ArrayList;

public class RatesListActivity extends BaseActivity {

    private SharedViewModel sharedViewModel;
    private RecyclerView recyclerView;
    private CurrencyAdapter currencyAdapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyAdapter = new CurrencyAdapter(new ArrayList<>());
        recyclerView.setAdapter(currencyAdapter);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        sharedViewModel.getFilteredCurrencies().observe(this, currencies -> {
            currencyAdapter.setCurrencies(currencies);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sharedViewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_rates_list;
    }
}
