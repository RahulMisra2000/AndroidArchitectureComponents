package com.pluralsight.cryptobam.viewmodel;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluralsight.cryptobam.MainActivity;
import com.pluralsight.cryptobam.entities.CryptoCoinEntity;
import com.pluralsight.cryptobam.recview.CoinModel;
import com.pluralsight.cryptobam.screens.MainScreen;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by omrierez on 23.11.17.
 */

public class CryptoViewModel {

    private static final String TAG = CryptoViewModel.class.getSimpleName();
    public final String CRYPTO_URL_PATH = "https://files.coinmarketcap.com/static/img/coins/128x128/%s.png";
    public final String ENDPOINT_FETCH_CRYPTO_DATA = "https://api.coinmarketcap.com/v1/ticker/?limit=100";
    private RequestQueue mQueue;
    private final ObjectMapper mObjMapper = new ObjectMapper();
    private MainScreen mView;
    private Context mAppContext;

    public CryptoViewModel() {
        Log.d(TAG, "NEW VIEWMODEL WAS CREATED");
    }
    public void bind(MainActivity view) {
        mView=view;
        mAppContext=view.getApplicationContext();

    }
    public void unbind()
    {
        mView=null;
    }
    private  class EntityToModelMapperTask extends AsyncTask<List<CryptoCoinEntity>, Void, List<CoinModel>> {
        @Override
        protected List<CoinModel> doInBackground(List<CryptoCoinEntity>... data) {
            final ArrayList<CoinModel> listData = new ArrayList<>();
            CryptoCoinEntity entity;
            for (int i = 0; i < data[0].size(); i++) {
                entity = data[0].get(i);
                listData.add(new CoinModel(entity.getName(), entity.getSymbol(), String.format(CRYPTO_URL_PATH, entity.getId()), entity.getPriceUsd(), entity.get24hVolumeUsd()));
            }

            return listData;
        }

        @Override
        protected void onPostExecute(List<CoinModel> data) {
            if (mView!=null)
                mView.updateData(data);

        }


    }
    private  Response.Listener<JSONArray> mResponseListener = response -> {
        writeDataToInternalStorage(response);
        ArrayList<CryptoCoinEntity> data = parseJSON(response.toString());
        Log.d(TAG, "data fetched:" + data);
        new EntityToModelMapperTask().execute(data);
    };

    private  Response.ErrorListener mErrorListener= error -> {
        if (mView!=null)
            mView.setError(error.toString());
        try {
            JSONArray data = readDataFromStorage();
            ArrayList<CryptoCoinEntity> entities = parseJSON(data.toString());
            new EntityToModelMapperTask().execute(entities);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    };
    private JsonArrayRequest mJsonObjReq;
    public void fetchData() {
        if (mQueue == null)
            mQueue = Volley.newRequestQueue(mAppContext);
        // Request a string response from the provided URL.
        mJsonObjReq = new JsonArrayRequest(ENDPOINT_FETCH_CRYPTO_DATA,
                mResponseListener,mErrorListener);
        // Add the request to the RequestQueue.
        mQueue.add(mJsonObjReq);
    }
    public ArrayList<CryptoCoinEntity> parseJSON(String jsonStr) {
        ArrayList<CryptoCoinEntity> data = null;

        try {
            data = mObjMapper.readValue(jsonStr, new TypeReference<ArrayList<CryptoCoinEntity>>() {
            });
        } catch (Exception e) {
            if (mView!=null)
                mView.setError(e.getMessage());
            e.printStackTrace();
        }
        return data;
    }
    //////////////////////////////////////////////////////////////////////////////////////STORAGE CODE///////////////////////////////////////////////////////////////////////////////////////////
    String DATA_FILE_NAME = "crypto.data";

    private void writeDataToInternalStorage(JSONArray data) {
        FileOutputStream fos = null;
        try {
            fos = mAppContext.openFileOutput(DATA_FILE_NAME, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(data.toString().getBytes());
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private JSONArray readDataFromStorage() throws JSONException {
        FileInputStream fis = null;
        try {
            fis = mAppContext.openFileInput(DATA_FILE_NAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray(sb.toString());
    }
}
