package com.example.konwerter3;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);
    }

    protected abstract int getLayoutId();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_converter) {
            startActivity(new Intent(this, CurrencyConverterActivity.class));
            return true;
        } else if (itemId == R.id.navigation_chart) {
            startActivity(new Intent(this, ChartAnalysisActivity.class));
            return true;
        } else if (itemId == R.id.navigation_rates_list) {
            startActivity(new Intent(this, RatesListActivity.class));
            return true;
        }
        return false;
    }
}
