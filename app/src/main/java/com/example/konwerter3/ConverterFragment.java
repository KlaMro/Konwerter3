package com.example.konwerter3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.konwerter3.databinding.FragmentConverterBinding;
import com.example.konwerter3.model.Currency;

public class ConverterFragment extends Fragment {

    private FragmentConverterBinding binding;
    private SharedViewModel sharedViewModel;
    private Currency selectedCurrency;
    private String conversionResultText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConverterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.getSelectedCurrency().observe(getViewLifecycleOwner(), currency -> {
            selectedCurrency = currency;
            if (currency != null) {
                binding.currencyNameTextView.setText(String.format("%s (%s)", currency.getCurrencyName(), currency.getCurrencyCode()));
                calculateConversion(binding.amountEditText.getText().toString());
            }
        });

        binding.amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateConversion(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.shareButton.setOnClickListener(v -> {
            if (conversionResultText != null && !conversionResultText.isEmpty()) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, conversionResultText);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        });
    }

    private void calculateConversion(String amountStr) {
        if (selectedCurrency == null) {
            binding.resultTextView.setText("Wybierz walutę z listy");
            binding.shareButton.setVisibility(View.GONE);
            return;
        }

        if (amountStr.isEmpty()) {
            binding.resultTextView.setText("");
            binding.shareButton.setVisibility(View.GONE);
            conversionResultText = null;
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            double result = amount * selectedCurrency.getExchangeRate();
            conversionResultText = String.format("%.2f %s = %.2f PLN", amount, selectedCurrency.getCurrencyCode(), result);
            binding.resultTextView.setText(conversionResultText);
            binding.shareButton.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            binding.resultTextView.setText("Błędna kwota");
            binding.shareButton.setVisibility(View.GONE);
            conversionResultText = null;
        }
    }
}
