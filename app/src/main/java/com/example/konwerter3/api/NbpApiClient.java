package com.example.konwerter3.api;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.example.konwerter3.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NbpApiClient {

    private static final String TAG = "NbpApiClient";
    private static final String API_TABLES_URL = "https://api.nbp.pl/api/exchangerates/tables/";
    private static final String API_RATES_URL = "https://api.nbp.pl/api/exchangerates/rates/";

    public interface NbpApiListener {
        void onRatesFetched(JSONArray rates);
        void onError(String message);
    }

    public interface NbpHistoryListener {
        void onHistoryFetched(JSONObject history);
        void onError(String message);
    }

    public void fetchCurrentRates(DatabaseHelper dbHelper, NbpApiListener listener) {
        new FetchRatesTask(dbHelper, listener, "A").execute();
    }

    public void fetchHistoricalRatesForCurrency(String currencyCode, String startDate, String endDate, DatabaseHelper dbHelper, NbpHistoryListener listener) {
        new FetchCurrencyHistoryTask(dbHelper, listener, "A", currencyCode, startDate, endDate).execute();
    }

    private static class FetchRatesTask extends AsyncTask<String, Void, String> {
        private NbpApiListener listener;
        private DatabaseHelper dbHelper;
        private String table;

        FetchRatesTask(DatabaseHelper dbHelper, NbpApiListener listener, String table) {
            this.dbHelper = dbHelper;
            this.listener = listener;
            this.table = table;
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = API_TABLES_URL + table + "/?format=json";
            return makeApiCall(urlString);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                try {
                    JSONArray tables = new JSONArray(s);
                    if (tables.length() > 0) {
                        JSONObject tableData = tables.getJSONObject(0);
                        JSONArray rates = tableData.getJSONArray("rates");
                        saveRatesToDb(rates);
                        if (listener != null) {
                            listener.onRatesFetched(rates);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Parsing Error", e);
                    if (listener != null) {
                        listener.onError("JSON Parsing Error");
                    }
                }
            } else {
                if (listener != null) {
                    listener.onError("Failed to fetch data");
                }
            }
        }

        private void saveRatesToDb(JSONArray rates) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (int i = 0; i < rates.length(); i++) {
                    JSONObject rateObject = rates.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_NAME, rateObject.getString("currency"));
                    values.put(DatabaseHelper.COLUMN_CODE, rateObject.getString("code"));
                    values.put(DatabaseHelper.COLUMN_RATE, rateObject.getDouble("mid"));
                    db.insertWithOnConflict(DatabaseHelper.TABLE_RATES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
                db.setTransactionSuccessful();
            } catch (JSONException e) {
                Log.e(TAG, "Error saving to DB", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private static class FetchCurrencyHistoryTask extends AsyncTask<String, Void, String> {
        private NbpHistoryListener listener;
        private String table;
        private String currencyCode;
        private String startDate;
        private String endDate;
        private DatabaseHelper dbHelper;

        FetchCurrencyHistoryTask(DatabaseHelper dbHelper, NbpHistoryListener listener, String table, String currencyCode, String startDate, String endDate) {
            this.dbHelper = dbHelper;
            this.listener = listener;
            this.table = table;
            this.currencyCode = currencyCode;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = API_RATES_URL + table + "/" + currencyCode + "/" + startDate + "/" + endDate + "/";
            return makeApiCall(urlString);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                try {
                    JSONObject historyData = new JSONObject(s);
                    saveHistoricalRatesToDb(historyData);
                    if (listener != null) {
                        listener.onHistoryFetched(historyData);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Parsing Error in History", e);
                    if (listener != null) {
                        listener.onError("JSON Parsing Error");
                    }
                }
            } else {
                if (listener != null) {
                    listener.onError("Failed to fetch history data");
                }
            }
        }

        private void saveHistoricalRatesToDb(JSONObject historyData) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                String code = historyData.getString("code");
                String currencyName = historyData.getString("currency");
                JSONArray rates = historyData.getJSONArray("rates");

                for (int i = 0; i < rates.length(); i++) {
                    JSONObject rateObject = rates.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_CODE, code);
                    values.put(DatabaseHelper.COLUMN_NAME, currencyName);
                    values.put(DatabaseHelper.COLUMN_RATE, rateObject.getDouble("mid"));
                    values.put(DatabaseHelper.COLUMN_DATE, rateObject.getString("effectiveDate"));
                    db.insertWithOnConflict(DatabaseHelper.TABLE_RATES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
                db.setTransactionSuccessful();
            } catch (JSONException e) {
                Log.e(TAG, "Error saving historical rates to DB", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private static String makeApiCall(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr = null;

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            jsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return jsonStr;
    }
}
