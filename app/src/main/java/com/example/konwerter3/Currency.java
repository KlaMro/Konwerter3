
package com.example.konwerter3;

public class Currency {
    private String code;
    private String currency;
    private double mid;
    private boolean isFavorite;

    public Currency(String code, String currency, double mid) {
        this.code = code;
        this.currency = currency;
        this.mid = mid;
        this.isFavorite = false; // Default to not favorite
    }

    public String getCode() {
        return code;
    }

    public String getCurrency() {
        return currency;
    }

    public double getMid() {
        return mid;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    // This is important for the ArrayAdapter to display the currency name in AutoCompleteTextView
    @Override
    public String toString() {
        return code + " - " + currency;
    }
}
