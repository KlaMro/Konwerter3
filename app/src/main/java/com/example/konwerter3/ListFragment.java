package com.example.konwerter3;

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
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.konwerter3.databinding.FragmentListBinding;
import com.example.konwerter3.model.Currency;

public class ListFragment extends Fragment implements CurrencyAdapter.OnItemClickListener {

    private FragmentListBinding binding;
    private SharedViewModel sharedViewModel;
    private CurrencyAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CurrencyAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        sharedViewModel.getFilteredCurrencies().observe(getViewLifecycleOwner(), currencies -> {
            adapter.setCurrencies(currencies);
        });

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sharedViewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        sharedViewModel.refreshCurrencies();
    }

    @Override
    public void onFavoriteClick(Currency currency) {
        sharedViewModel.toggleFavorite(currency);
    }

    @Override
    public void onItemClick(Currency currency) {
        sharedViewModel.setSelectedCurrency(currency);
        ViewPager2 viewPager = getActivity().findViewById(R.id.view_pager);
        viewPager.setCurrentItem(0);
    }
}
