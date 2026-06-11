package com.example.konwerter3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konwerter3.model.Currency;

import java.util.Collections;
import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder> {

    private List<Currency> currencies = Collections.emptyList();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onFavoriteClick(Currency currency);
        void onItemClick(Currency currency);
    }

    public CurrencyAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_currency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Currency currency = currencies.get(position);
        holder.code.setText(currency.getCurrencyCode());
        holder.name.setText(currency.getCurrencyName());
        holder.rate.setText(String.format("%.4f", currency.getExchangeRate()));

        if (currency.isFavorite()) {
            holder.star.setImageResource(R.drawable.ic_star_filled);
        } else {
            holder.star.setImageResource(R.drawable.ic_star_outline);
        }

        holder.star.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(currency);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currency);
            }
        });
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView code, name, rate;
        ImageView star;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.currency_code);
            name = itemView.findViewById(R.id.currency_name);
            rate = itemView.findViewById(R.id.currency_rate);
            star = itemView.findViewById(R.id.favorite_star);
        }
    }
}
