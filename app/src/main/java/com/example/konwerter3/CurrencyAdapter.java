package com.example.konwerter3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder> {

    private List<Currency> currencies;
    private LayoutInflater inflater;
    private FavoriteClickListener listener;

    public interface FavoriteClickListener {
        void onFavoriteClick(Currency currency);
    }

    public CurrencyAdapter(Context context, List<Currency> currencies, FavoriteClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.currencies = currencies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_currency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Currency currency = currencies.get(position);
        holder.code.setText(currency.getCode());
        holder.name.setText(currency.getName());
        holder.rate.setText(String.format("1 %s = %.4f PLN", currency.getCode(), currency.getMid()));

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
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    public void updateData(List<Currency> newCurrencies) {
        this.currencies = newCurrencies;
        notifyDataSetChanged(); // For simplicity. For performance, use DiffUtil.
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
