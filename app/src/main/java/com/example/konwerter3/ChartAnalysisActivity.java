
package com.example.konwerter3;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;

public class ChartAnalysisActivity extends BaseActivity {

    private CurrencyViewModel currencyViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currencyViewModel = new ViewModelProvider(this).get(CurrencyViewModel.class);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chart_analysis;
    }
}
