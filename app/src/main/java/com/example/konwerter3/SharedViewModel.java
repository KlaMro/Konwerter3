package com.example.konwerter3;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.konwerter3.model.Currency;
import com.example.konwerter3.db.CurrencyRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SharedViewModel extends AndroidViewModel {

    private CurrencyRepository repository;
    private LiveData<List<Currency>> allCurrencies;
    private MediatorLiveData<List<Currency>> filteredCurrencies = new MediatorLiveData<>();
    private MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private MutableLiveData<Currency> selectedCurrency = new MutableLiveData<>();

    public SharedViewModel(Application application) {
        super(application);
        repository = new CurrencyRepository(application);
        allCurrencies = repository.getAllCurrencies();

        filteredCurrencies.addSource(allCurrencies, currencies -> filterAndSortList(searchQuery.getValue(), currencies));
        filteredCurrencies.addSource(searchQuery, query -> filterAndSortList(query, allCurrencies.getValue()));
    }

    public LiveData<List<Currency>> getFilteredCurrencies() {
        return filteredCurrencies;
    }

    public LiveData<Currency> getSelectedCurrency() {
        return selectedCurrency;
    }

    public void setSelectedCurrency(Currency currency) {
        selectedCurrency.setValue(currency);
    }

    public void refreshCurrencies() {
        repository.refreshCurrencies();
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void toggleFavorite(Currency currency) {
        currency.setFavorite(!currency.isFavorite());
        repository.updateCurrency(currency);
    }

    private void filterAndSortList(String query, List<Currency> currencies) {
        if (currencies == null) {
            return;
        }

        List<Currency> filteredList;
        if (query == null || query.isEmpty()) {
            filteredList = currencies;
        } else {
            String lowerCaseQuery = query.toLowerCase();
            filteredList = currencies.stream()
                    .filter(c -> c.getCurrencyCode().toLowerCase().contains(lowerCaseQuery) || c.getCurrencyName().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        Collections.sort(filteredList, (o1, o2) -> {
            if (o1.isFavorite() && !o2.isFavorite()) {
                return -1;
            }
            if (!o1.isFavorite() && o2.isFavorite()) {
                return 1;
            }
            return o1.getCurrencyCode().compareTo(o2.getCurrencyCode());
        });

        filteredCurrencies.setValue(filteredList);
    }
}
