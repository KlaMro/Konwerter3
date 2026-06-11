package com.example.konwerter3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.konwerter3.api.NbpApiClient;
import com.example.konwerter3.model.Currency;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CurrencyRepository {

    private DatabaseHelper dbHelper;
    private NbpApiClient apiClient;
    private MutableLiveData<List<Currency>> currencyListLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> dataDateLiveData = new MutableLiveData<>();
    private Set<String> favoriteCurrencies = new HashSet<>();

    public CurrencyRepository(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
        apiClient = new NbpApiClient();
        loadFavoriteCurrencies();
    }

    public LiveData<List<Currency>> getCurrencyList() {
        return currencyListLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<String> getDataDate() {
        return dataDateLiveData;
    }

    public void fetchRates(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            apiClient.fetchCurrentRates(dbHelper, new NbpApiClient.NbpApiListener() {
                @Override
                public void onRatesFetched(JSONArray rates) {
                    try {
                        List<Currency> currencies = parseRates(rates);
                        currencyListLiveData.postValue(currencies);
                        // We need to get the date from the API response structure if possible
                        // For now, we will rely on the date stored in the DB
                    } catch (JSONException e) {
                        errorLiveData.postValue("Error parsing data");
                    }
                }

                @Override
                public void onError(String message) {
                    loadRatesFromDatabase(false);
                }
            });
        } else {
            loadRatesFromDatabase(true);
        }
    }

    private List<Currency> parseRates(JSONArray rates) throws JSONException {
        List<Currency> currencies = new ArrayList<>();
        Currency pln = new Currency("PLN", "Polish Zloty", 1.0);
        if (favoriteCurrencies.contains("PLN")) pln.setFavorite(true);
        currencies.add(pln);

        for (int i = 0; i < rates.length(); i++) {
            JSONObject rate = rates.getJSONObject(i);
            String code = rate.getString("code");
            Currency currency = new Currency(
                code,
                rate.getString("currency"),
                rate.getDouble("mid")
            );
            if (favoriteCurrencies.contains(code)) {
                currency.setFavorite(true);
            }
            currencies.add(currency);
        }
        sortCurrencies(currencies);
        return currencies;
    }

    private void loadRatesFromDatabase(boolean isOffline) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor dateCursor = db.query(DatabaseHelper.TABLE_RATES, new String[]{DatabaseHelper.COLUMN_DATE}, null, null, null, null, DatabaseHelper.COLUMN_DATE + " DESC", "1");
        String lastDate = null;
        if (dateCursor.moveToFirst()) {
            lastDate = dateCursor.getString(0);
            dataDateLiveData.postValue(lastDate);
        }
        dateCursor.close();

        if (lastDate == null) {
            errorLiveData.postValue("No data. Connect to internet.");
            currencyListLiveData.postValue(new ArrayList<>()); // Post empty list
            return;
        }

        if (isOffline) {
             errorLiveData.postValue("Offline. Data from: " + lastDate);
        } else {
             errorLiveData.postValue("API Error. Data from: " + lastDate);
        }

        Cursor cursor = db.query(DatabaseHelper.TABLE_RATES, null, DatabaseHelper.COLUMN_DATE + " = ?", new String[]{lastDate}, null, null, null);
        List<Currency> currencies = new ArrayList<>();
        Currency pln = new Currency("PLN", "Polish Zloty", 1.0);
        if (favoriteCurrencies.contains("PLN")) pln.setFavorite(true);
        currencies.add(pln);

        while (cursor.moveToNext()) {
            String code = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CODE));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            double rate = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RATE));
            Currency currency = new Currency(code, name, rate);
            if (favoriteCurrencies.contains(code)) {
                currency.setFavorite(true);
            }
            currencies.add(currency);
        }
        cursor.close();
        sortCurrencies(currencies);
        currencyListLiveData.postValue(currencies);
    }
    
    private void loadFavoriteCurrencies() {
        favoriteCurrencies = new HashSet<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FAVORITES, new String[]{DatabaseHelper.COLUMN_FAV_CODE}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            favoriteCurrencies.add(cursor.getString(0));
        }
        cursor.close();
    }

    public void toggleFavorite(Currency currency) {
        boolean isFavorite = !currency.isFavorite();
        currency.setFavorite(isFavorite);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (isFavorite) {
            favoriteCurrencies.add(currency.getCode());
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_FAV_CODE, currency.getCode());
            db.insert(DatabaseHelper.TABLE_FAVORITES, null, values);
        } else {
            favoriteCurrencies.remove(currency.getCode());
            db.delete(DatabaseHelper.TABLE_FAVORITES, DatabaseHelper.COLUMN_FAV_CODE + " = ?", new String[]{currency.getCode()});
        }
        
        List<Currency> currentList = currencyListLiveData.getValue();
        if (currentList != null) {
            sortCurrencies(currentList);
            currencyListLiveData.postValue(new ArrayList<>(currentList)); // Post a new list to trigger observers
        }
    }

    private void sortCurrencies(List<Currency> currencies) {
        Collections.sort(currencies, (o1, o2) -> {
            if (o1.isFavorite() && !o2.isFavorite()) return -1;
            if (!o1.isFavorite() && o2.isFavorite()) return 1;
            return o1.getCode().compareTo(o2.getCode());
        });
    }
}
