package com.cryptoboom.data.repository.datasource;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by omrierez on 28.12.17.
 */

public class LocalDataSource implements DataSource<JSONArray>{
    private final static String DATA_FILE_NAME = "crypto.data";
    private final Context mAppContext;
    private final MutableLiveData<String> mError=new MutableLiveData<>();
    private final MutableLiveData<JSONArray> mData=new MutableLiveData<>();
    public LocalDataSource(Context mAppContext) {
        this.mAppContext = mAppContext;
    }
    @Override
    public LiveData<JSONArray> getDataStream() {
        return mData;
    }
    @Override
    public LiveData<String> getErrorStream() {
        return mError;
    }
    public void fetch()
    {
        try {
            mData.setValue(readData());
        } catch (JSONException e) {
            e.printStackTrace();
            mError.setValue(e.getMessage());
        }
    }
    public void writeData(String data) {
        FileOutputStream fos = null;
        try {
            fos = mAppContext.openFileOutput(DATA_FILE_NAME, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(data.getBytes());
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray readData() throws JSONException {
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
