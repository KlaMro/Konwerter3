
package com.example.konwerter3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "app_database.db";
    // Incremented the database version to trigger onUpgrade
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_RATES = "currency_rates";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_NAME = "currency_name"; // New column
    public static final String COLUMN_RATE = "rate";
    public static final String COLUMN_DATE = "publication_date";

    public static final String TABLE_FAVORITES = "favorite_currencies";
    public static final String COLUMN_FAV_CODE = "currency_code";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_CURRENCY_TABLE = "CREATE TABLE " + TABLE_RATES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CODE + " TEXT NOT NULL, " +
                COLUMN_NAME + " TEXT NOT NULL, " + // Add new column to create statement
                COLUMN_RATE + " REAL NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                "UNIQUE (" + COLUMN_CODE + ", " + COLUMN_DATE + ") ON CONFLICT REPLACE" +
                ");";

        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_FAV_CODE + " TEXT PRIMARY KEY NOT NULL");";

        db.execSQL(SQL_CREATE_CURRENCY_TABLE);
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }
}
