package com.pluralsight.cryptobam.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pluralsight.cryptobam.viewmodel.CryptoViewModel;

/**
 * Created by omrierez on 02.12.17.
 */

public class UILessFragment extends android.support.v4.app.Fragment {
    private static final String TAG = UILessFragment.class.getSimpleName();
    private CryptoViewModel mViewModel;
    private final Observer<Double> mObserver= totalMarketCap ->
            Log.d(TAG, "onChanged() called with: aDouble = [" +totalMarketCap + "]");

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //mViewModel = ViewModelProviders.of(this).get(CryptoViewModel.class);

        mViewModel = ViewModelProviders.of(getActivity()).get(CryptoViewModel.class);

        mViewModel.getTotalMarketCap().observe(this,mObserver);

    }
}
