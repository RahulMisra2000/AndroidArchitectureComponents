package com.pluralsight.cryptobam;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recView;
    private MyCryptoAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        fetchData();
        mTracker = new Tracker(this);
        mTracker.trackOnCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mTracker.trackOnStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.trackOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTracker.trackOnPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTracker.trackOnStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTracker.trackOnDestroy();
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

    public class Divider extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public Divider(Context context) {
            mDivider = context.getResources().getDrawable(R.drawable.list_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    private static class MyCryptoAdapter extends RecyclerView.Adapter<MyCryptoAdapter.CoinViewHolder> {

        List<CoinModel> mItems = new ArrayList<>();
        public final String STR_TEMPLATE_NAME = "%s\t\t\t\t\t\t%s";
        public final String STR_TEMPLATE_PRICE = "%s$\t\t\t\t\t\t24H Volume:\t\t\t%s$";


        @Override
        public void onBindViewHolder(CoinViewHolder holder, int position) {
            final CoinModel model = mItems.get(position);
            holder.tvNameAndSymbol.setText(String.format(STR_TEMPLATE_NAME, model.name, model.symbol));
            holder.tvPriceAndVolume.setText(String.format(STR_TEMPLATE_PRICE, model.priceUsd, model.volume24H));
            Glide.with(holder.ivIcon).load(model.imageUrl).into(holder.ivIcon);
        }

        @Override
        public CoinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new CoinViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false));
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void setItems(List<CoinModel> items) {
            this.mItems.clear();
            this.mItems.addAll(items);

        }


        class CoinViewHolder extends RecyclerView.ViewHolder {

            TextView tvNameAndSymbol;
            TextView tvPriceAndVolume;
            ImageView ivIcon;

            public CoinViewHolder(View itemView) {
                super(itemView);
                tvNameAndSymbol = itemView.findViewById(R.id.tvNameAndSymbol);
                tvPriceAndVolume = itemView.findViewById(R.id.tvPriceAndVolume);
                ivIcon = itemView.findViewById(R.id.ivIcon);
            }
        }
    }


    private void showErrorToast(String error) {
        Toast.makeText(this, "Error:" + error, Toast.LENGTH_SHORT).show();
    }


    private class CoinModel {
        public final String name;
        public final String symbol;
        public final String imageUrl;
        public final String priceUsd;
        public final String volume24H;

        public CoinModel(String name, String symbol, String imageUrl, String priceUsd, String volume24H) {
            this.name = name;
            this.symbol = symbol;
            this.imageUrl = imageUrl;
            this.priceUsd = priceUsd;
            this.volume24H = volume24H;
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////NETWORK RELATED CODE///////////////////////////////////////////////////////////////////////////////////////


    public final String CRYPTO_URL_PATH = "https://files.coinmarketcap.com/static/img/coins/128x128/%s.png";
    public final String ENDPOINT_FETCH_CRYPTO_DATA = "https://api.coinmarketcap.com/v1/ticker/?limit=100";
    private RequestQueue mQueue;
    private final ObjectMapper mObjMapper = new ObjectMapper();

    private void fetchData() {
        if (mQueue == null)
            mQueue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        final JsonArrayRequest jsonObjReq = new JsonArrayRequest(ENDPOINT_FETCH_CRYPTO_DATA,
                response -> {
                    writeDataToInternalStorage(response);
                    ArrayList<CryptoCoinEntity> data = parseJSON(response.toString());
                    Log.d(TAG, "data fetched:" + data);
                    new EntityToModelMapperTask().execute(data);


                },
                error -> {
                    showErrorToast(error.toString());
                    try {
                        JSONArray data = readDataFromStorage();
                        ArrayList<CryptoCoinEntity> entities = parseJSON(data.toString());
                        new EntityToModelMapperTask().execute(entities);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

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
            showErrorToast(e.getMessage());
            e.printStackTrace();
        }
        return data;
    }



    private class EntityToModelMapperTask extends AsyncTask<List<CryptoCoinEntity>, Void, List<CoinModel>> {
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
    ///////////////////////////////////////////////////////////////////////////////////////////TRACKING CODE/////////////////////////////////////////////////////////////////////////////////////





    static class Tracker {

        private static final String TAG = Tracker.class.getSimpleName();
        private final String TRACKING_URL = "https://www.google.com";
        private final Context con;
        private final RequestQueue mQueue;
        private final String mOsVersion;

        public Tracker(Context con) {
            this.con = con;
            mOsVersion = Build.VERSION.RELEASE;
            mQueue = Volley.newRequestQueue(con);

        }

        private StringRequest generateTrackingStringRequest(final String eventName) {
            return new StringRequest(Request.Method.POST, TRACKING_URL,
                    response -> {
                        Log.d(TAG, "onResponse() called with: response = [" + response + "]");

                    },
                    error -> Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]")) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("eventName", eventName);
                    params.put("osVersion", mOsVersion);
                    return new JSONObject(params).toString().getBytes();
                }
            };
        }

        public void trackOnCreate() {

            mQueue.add(generateTrackingStringRequest("create"));
        }

        public void trackOnDestroy() {

            mQueue.add(generateTrackingStringRequest("destroy"));

        }

        public void trackOnStart() {

            mQueue.add(generateTrackingStringRequest("start"));

        }

        public void trackOnResume() {

            mQueue.add(generateTrackingStringRequest("resume"));

        }

        public void trackOnPause() {

            mQueue.add(generateTrackingStringRequest("pause"));

        }

        public void trackOnStop() {

            mQueue.add(generateTrackingStringRequest("stop"));

        }

        public void trackLocation(int lat, int lng) {
            mQueue.add(generateTrackingStringRequest("location\t" + lat + "-" + lng));

        }
    }





    ////////////////////////////////////////////////////////////////////////////////////LOCATION RELATED CODE/////////////////////////////////////////////////////////////////////////////////////
    private final static int PERMISSION_REQUEST_LOCATION = 1234;

    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Please give me location permissions", Toast.LENGTH_SHORT).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "onConnected() called with: bundle = [" + bundle + "]");
                        mLocationRequest = new LocationRequest();
                        mLocationRequest.setInterval(10000); // two minute interval
                        mLocationRequest.setFastestInterval(10000);
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallbacks, Looper.myLooper());
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended() called with: i = [" + i + "]");
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> Log.d(TAG, "onConnectionFailed() called with: connectionResult = [" + connectionResult + "]"))
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private LocationCallback mLocationCallbacks = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "onLocationResult() called with: locationResult = [" + locationResult + "]");
            for (Location location : locationResult.getLocations()) {
                if (location != null) {
                    int lat = (int) (location.getLatitude());
                    int lng = (int) (location.getLongitude());
                    mTracker.trackLocation(lat, lng);
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
