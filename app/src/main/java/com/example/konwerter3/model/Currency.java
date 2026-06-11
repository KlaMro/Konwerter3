package com.example.konwerter3.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "currencies")
public class Currency {

    @PrimaryKey
    @NotNull
    private String code;

    private String name;

    private double mid;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    public Currency(String code, String name, double mid) {
        this.code = code;
        this.name = name;
        this.mid = mid;
        this.isFavorite = false;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMid() {
        return mid;
    }

    public void setMid(double mid) {
        this.mid = mid;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
