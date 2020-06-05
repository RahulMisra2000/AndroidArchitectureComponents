package com.cryptoboom.data;

import com.cryptoboom.data.entities.CryptoCoinEntity;
import com.cryptoboom.data.mappers.CryptoMapper;
import com.cryptoboom.data.models.CoinModel;

import org.junit.Test;

import java.nio.charset.Charset;
import java.util.List;

import cryptobam.utils.CoinEntityGenerator;
import cryptobam.utils.CoinEntityMatcher;

import static junit.framework.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CryptoMapperUnitTest {
    private static final String INPUT_FILE_PATH =
            "/Users/omrierez/AndroidStudioProjects/CryptoBoom/data/src/sharedTest/testsets/coins.json";
    @Test
    public void mapperFuncJsonToEntity() throws Exception {
        CryptoMapper mapper = new CryptoMapper();
        String dataStr = CoinEntityGenerator.readFile(INPUT_FILE_PATH, Charset.defaultCharset());
        List<CryptoCoinEntity> coins = CoinEntityGenerator.createConstantCoinsDataSet(INPUT_FILE_PATH);
        List<CryptoCoinEntity> result = mapper.mapJSONToEntity(dataStr);
        final CoinEntityMatcher matcher=new CoinEntityMatcher(coins);
        assertTrue(matcher.matches(result));
    }
    @Test
    public void mapperFuncEntitiesToModels() throws Exception {
        CryptoMapper mapper = new CryptoMapper();
        List<CryptoCoinEntity> data = CoinEntityGenerator.createConstantCoinsDataSet(INPUT_FILE_PATH);
        List<CoinModel> dataModels = mapper.mapEntityToModel(data);
        assertTrue(compareEntitiesToModels(dataModels,data));
    }
    private boolean compareEntitiesToModels(List<CoinModel> models, List<CryptoCoinEntity> entities)
    {
        if(models.size()!= entities.size())
            return false;

        final int size=models.size();
        CoinModel model;
        CryptoCoinEntity entity;
        for (int i = 0; i <size; i++) {
            model=models.get(i);
            entity=entities.get(i);
            if (model.name.compareTo(entity.getName())==0
                    &&model.symbol.compareTo(entity.getSymbol())==0
                    &&model.priceUsd.compareTo(entity.getPriceUsd())==0);
            else
                return false;
        }
        return true;
    }
}