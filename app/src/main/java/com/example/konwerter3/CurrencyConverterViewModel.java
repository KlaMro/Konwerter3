package com.example.konwerter3;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CurrencyConverterViewModel extends AndroidViewModel {

    private CurrencyRepository repository;
    private LiveData<List<Currency>> allCurrencies;
    private MediatorLiveData<List<Currency>> filteredCurrencies = new MediatorLiveData<>();
    private MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    public CurrencyConverterViewModel(Application application) {
        super(application);
        repository = new CurrencyRepository(application);
        allCurrencies = repository.getCurrencyList();

        filteredCurrencies.addSource(allCurrencies, currencies -> filterList(searchQuery.getValue(), currencies));
        filteredCurrencies.addSource(searchQuery, query -> filterList(query, allCurrencies.getValue()));
    }

    public LiveData<List<Currency>> getFilteredCurrencies() {
        return filteredCurrencies;
    }

    public LiveData<String> getError() {
        return repository.getError();
    }

    public void fetchRates(boolean isNetworkAvailable) {
        repository.fetchRates(isNetworkAvailable);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    private void filterList(String query, List<Currency> currencies) {
        if (currencies == null) {
            return;
        }
        if (query == null || query.isEmpty()) {
            filteredCurrencies.setValue(currencies);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            List<Currency> filteredList = currencies.stream()
                    .filter(c -> c.getCode().toLowerCase().contains(lowerCaseQuery) || c.getName().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
            filteredCurrencies.setValue(filteredList);
        }
    }

    public void toggleFavorite(Currency currency) {
        repository.toggleFavorite(currency);
    }
}
