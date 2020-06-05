package com.pluralsight.cryptobam;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.pluralsight.cryptobam.tracking.Tracker;

/**
 * Created by omrierez on 02.11.17.
 */

public class TrackingActivity extends AppCompatActivity {


    protected Tracker mTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker=new Tracker(this);
    }
}
