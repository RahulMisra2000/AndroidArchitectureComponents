package com.pluralsight.cryptobam.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluralsight.cryptobam.entities.CryptoCoinEntity;
import com.pluralsight.cryptobam.recview.CoinModel;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by omrierez on 10/1/17.
 */

public class CryptoViewModel extends ViewModel {
    private static final String TAG = CryptoViewModel.class.getSimpleName();

    private MutableLiveData<List<CoinModel>> mDataApi = new MutableLiveData<>();

    private MutableLiveData<String> mError = new MutableLiveData<>();

    private ExecutorService mExecutor = Executors.newFixedThreadPool(5);

    private Context mAppContext;

    public LiveData<List<CoinModel>> getCoinsMarketData() {
        return mDataApi;
    }

    public LiveData<String> getErrorUpdates() {
        return mError;
    }

    public LiveData<Double> getTotalMarketCap() {

        return Transformations.map(mDataApi, input -> {
            double totalMarketCap = 0;
            for (int i = 0; i < input.size(); i++) {
                totalMarketCap += input.get(i).marketCap;
            }
            return totalMarketCap;
        });
    }

    public CryptoViewModel() {
        super();
        Log.d(TAG, "NEW VIEWMODEL IS CREATED");
    }

    public void setAppContext(Context mAppContext) {
        this.mAppContext = mAppContext;
        if (mQueue == null)
            mQueue = Volley.newRequestQueue(mAppContext);
        fetchData();
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     * <p>
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    @Override
    protected void onCleared() {
        Log.d(TAG, "onCleared() called");
        super.onCleared();
    }

    ////////////////////////////////////////////////////////////////////////////////////NETWORK RELATED CODE///////////////////////////////////////////////////////////////////////////////////////


    public final String CRYPTO_URL_PATH = "https://files.coinmarketcap.com/static/img/coins/128x128/%s.png";
    public final String ENDPOINT_FETCH_CRYPTO_DATA = "https://api.coinmarketcap.com/v1/ticker/?limit=100";
    private RequestQueue mQueue;
    private final ObjectMapper mObjMapper = new ObjectMapper();

    @NonNull
    private List<CoinModel> mapEntityToModel(List<CryptoCoinEntity> datum) {
        final ArrayList<CoinModel> listData = new ArrayList<>();
        CryptoCoinEntity entity;
        for (int i = 0; i < datum.size(); i++) {
            entity = datum.get(i);
            listData.add(new CoinModel(entity.getName(), entity.getSymbol(), String.format(CRYPTO_URL_PATH, entity.getId()),entity.getPriceUsd(),
                    entity.get24hVolumeUsd(), Double.valueOf(entity.getMarketCapUsd())));
        }

        return listData;
    }

    public void fetchData() {
        final JsonArrayRequest jsonObjReq =
                new JsonArrayRequest(ENDPOINT_FETCH_CRYPTO_DATA,
                response -> {
                    Log.d(TAG, "Thread->" +
                            Thread.currentThread().getName()+"\tGot some network response");
                    writeDataToInternalStorage(response);
                    final ArrayList<CryptoCoinEntity> data = parseJSON(response.toString());
                    List<CoinModel> mappedData = mapEntityToModel(data);
                    mDataApi.setValue(mappedData);
                },
                error -> {
                    Log.d(TAG, "Thread->" +
                            Thread.currentThread().getName()+"\tGot network error");
                    mError.setValue(error.toString());
                    mExecutor.execute(() -> {
                        try {
                            Log.d(TAG, "Thread->"+Thread.currentThread().getName()+
                                    "\tNot fetching from network because of network error - fetching from disk");
                            JSONArray data = readDataFromStorage();
                            ArrayList<CryptoCoinEntity> entities = parseJSON(data.toString());
                            List<CoinModel> mappedData = mapEntityToModel(entities);
                            mDataApi.postValue(mappedData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });


                });
        // Add the request to the RequestQueue.
        mQueue.add(jsonObjReq);
    }


    public ArrayList<CryptoCoinEntity> parseJSON(String jsonStr) {
        ArrayList<CryptoCoinEntity> data = null;

        try {
            data = mObjMapper.readValue(jsonStr, new TypeReference<ArrayList<CryptoCoinEntity>>() {
            });
        } catch (Exception e) {
            mError.postValue(e.toString());
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

