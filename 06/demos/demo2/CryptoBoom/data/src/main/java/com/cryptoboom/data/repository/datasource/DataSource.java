package com.cryptoboom.data.repository.datasource;

import android.arch.lifecycle.LiveData;

/**
 * Created by omrierez on 28.12.17.
 */

public interface DataSource<T> {

    LiveData<T> getDataStream();
    LiveData<String> getErrorStream();
}
