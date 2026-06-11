package com.example.konwerter3.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.konwerter3.model.Currency;

import java.util.List;

@Dao
public interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCurrencies(List<Currency> currencies);

    @Query("SELECT * FROM currencies")
    LiveData<List<Currency>> getAllCurrencies();

    @Query("DELETE FROM currencies")
    void deleteAllCurrencies();

    @Update
    void updateCurrency(Currency currency);
}
