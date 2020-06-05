package cryptobam.utils;

import com.cryptoboom.data.entities.CryptoCoinEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by omrierez on 21.01.18.
 */

public class CoinEntityGenerator {

    private final static Random rand = new Random();
    private static int id = 0;

    public static CryptoCoinEntity createRandomEntity() {
        CryptoCoinEntity entity = new CryptoCoinEntity();
        entity.setId(String.valueOf(id++));
        entity.setSymbol(UUID.randomUUID().toString().substring(0, 5));
        entity.setName("Bitcoin");
        entity.setRank(String.valueOf(rand.nextInt()));
        entity.setPriceUsd(String.valueOf(rand.nextFloat()));
        entity.setMarketCapUsd(String.valueOf(rand.nextInt(100)));
        return entity;
    }


    public static List<CryptoCoinEntity> createConstantCoinsDataSet(String filePath) throws IOException {
        ObjectMapper objMapper = new ObjectMapper();
        final String dataStr =readFile(filePath,Charset.defaultCharset());
        final List<CryptoCoinEntity> data =
                objMapper.readValue(dataStr, new TypeReference<ArrayList<CryptoCoinEntity>>() {});
        return data;
    }

   public static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
