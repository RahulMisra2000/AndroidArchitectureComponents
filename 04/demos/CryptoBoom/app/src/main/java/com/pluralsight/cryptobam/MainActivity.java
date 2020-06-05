package com.pluralsight.cryptobam;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluralsight.cryptobam.entities.CryptoCoinEntity;
import com.pluralsight.cryptobam.recview.CoinModel;
import com.pluralsight.cryptobam.recview.Divider;
import com.pluralsight.cryptobam.recview.MyCryptoAdapter;

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

public class MainActivity extends LocationActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recView;
    private MyCryptoAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        fetchData();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        recView = findViewById(R.id.recView);
        mSwipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            fetchData();
        });
        mAdapter = new MyCryptoAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        recView.setLayoutManager(lm);
        recView.setAdapter(mAdapter);
        recView.addItemDecoration(new Divider(this));
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> recView.smoothScrollToPosition(0));
    }

    private void showErrorToast(String error) {
        Toast.makeText(this, "Error:" + error, Toast.LENGTH_SHORT).show();
    }
    ////////////////////////////////////////////////////////////////////////////////////NETWORK RELATED CODE///////////////////////////////////////////////////////////////////////////////////////
    public final String CRYPTO_URL_PATH = "https://files.coinmarketcap.com/static/img/coins/128x128/%s.png";
    public final String ENDPOINT_FETCH_CRYPTO_DATA = "https://api.coinmarketcap.com/v1/ticker/?limit=100";
    private RequestQueue mQueue;
    private final ObjectMapper mObjMapper = new ObjectMapper();
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
            mAdapter.setItems(data);
            mSwipeRefreshLayout.setRefreshing(false);

        }


    }
    private  Response.Listener<JSONArray> mResponseListener = response -> {
        writeDataToInternalStorage(response);
        ArrayList<CryptoCoinEntity> data = parseJSON(response.toString());
        Log.d(TAG, "data fetched:" + data);
        new EntityToModelMapperTask().execute(data);
    };

    private  Response.ErrorListener mErrorListener= error -> {
        showErrorToast(error.toString());
        try {
            JSONArray data = readDataFromStorage();
            ArrayList<CryptoCoinEntity> entities = parseJSON(data.toString());
            new EntityToModelMapperTask().execute(entities);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    };
    private JsonArrayRequest mJsonObjReq;
    private void fetchData() {
        if (mQueue == null)
            mQueue = Volley.newRequestQueue(this);
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
            showErrorToast(e.getMessage());
            e.printStackTrace();
        }
        return data;
    }
    //////////////////////////////////////////////////////////////////////////////////////STORAGE CODE///////////////////////////////////////////////////////////////////////////////////////////
    String DATA_FILE_NAME = "crypto.data";

    private void writeDataToInternalStorage(JSONArray data) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(DATA_FILE_NAME, Context.MODE_PRIVATE);
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
            fis = openFileInput(DATA_FILE_NAME);
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
