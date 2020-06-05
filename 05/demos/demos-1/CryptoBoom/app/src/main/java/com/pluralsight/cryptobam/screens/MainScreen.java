package com.pluralsight.cryptobam.screens;

import com.pluralsight.cryptobam.recview.CoinModel;

import java.util.List;

/**
 * Created by omrierez on 23.11.17.
 */

public interface MainScreen {

    void updateData(List<CoinModel> data);
    void setError(String msg);
}
