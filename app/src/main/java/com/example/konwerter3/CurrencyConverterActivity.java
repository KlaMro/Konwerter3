package com.example.konwerter3;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class CurrencyConverterActivity extends AppCompatActivity implements CurrencyAdapter.FavoriteClickListener {

    private CurrencyConverterViewModel viewModel;
    private RecyclerView recyclerView;
    private CurrencyAdapter currencyAdapter;
    private TextInputEditText searchEditText;
    private LinearLayout emptyView;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        viewModel = new ViewModelProvider(this).get(CurrencyConverterViewModel.class);

        setupUI();
        observeViewModel();

        viewModel.fetchRates(NetworkUtils.isNetworkAvailable(this));
    }

    private void setupUI() {
        recyclerView = findViewById(R.id.recycler_view);
        searchEditText = findViewById(R.id.search_edit_text);
        emptyView = findViewById(R.id.empty_view);
        retryButton = findViewById(R.id.retry_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyAdapter = new CurrencyAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(currencyAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        retryButton.setOnClickListener(v -> viewModel.fetchRates(NetworkUtils.isNetworkAvailable(this)));
    }

    private void observeViewModel() {
        viewModel.getFilteredCurrencies().observe(this, currencies -> {
            if (currencies != null && !currencies.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                currencyAdapter.updateData(currencies);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFavoriteClick(Currency currency) {
        viewModel.toggleFavorite(currency);
    }
}
