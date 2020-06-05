package com.cryptoboom.data.dao;

import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cryptoboom.data.db.CoinDao;
import com.cryptoboom.data.db.RoomDb;
import com.cryptoboom.data.entities.CryptoCoinEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cryptobam.utils.CoinEntityMatcher;

import static cryptobam.utils.CoinEntityGenerator.createRandomEntity;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Created by omrierez on 21.01.18.
 */
@RunWith(AndroidJUnit4.class)

public class CoinDaoTest {
    private static final String TAG = CoinDaoTest.class.getSimpleName();
    private final int NUM_OF_INSERT_COINS = 100;

    private RoomDb cryptoDb;
    private CoinDao coinDao;
    @Mock
    private Observer<List<CryptoCoinEntity>> observer;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        Context context = InstrumentationRegistry.getTargetContext();
        cryptoDb = Room.inMemoryDatabaseBuilder(context, RoomDb.class)
                .allowMainThreadQueries().build();
        coinDao = cryptoDb.coinDao();
    }

    @After
    public void clean() throws Exception {
        cryptoDb.close();
    }


    private List<CryptoCoinEntity> createRandomCoins() {
        List<CryptoCoinEntity> coins = new ArrayList<>();
        for (int i = 0; i < NUM_OF_INSERT_COINS; i++)
            coins.add(createRandomEntity());
        return coins;
    }

    @Test
    public void insertCoinsEmpty() throws InterruptedException {
        List empty=new ArrayList();
        CountDownLatch latch = new CountDownLatch(1);
        coinDao.getAllCoinsLive().observeForever(observer);
        coinDao.insertCoins(new ArrayList<>());
        latch.await(50, TimeUnit.MILLISECONDS);
        verify(observer).onChanged(empty);

    }

    @Test
    public void insertCoins() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        List<CryptoCoinEntity> coins=createRandomCoins();
        coinDao.getAllCoinsLive().observeForever(observer);
        coinDao.insertCoins(coins);
        latch.await(1, TimeUnit.SECONDS);
        verify(observer,atLeastOnce()).onChanged(argThat(new CoinEntityMatcher(coins)));



    }


}
