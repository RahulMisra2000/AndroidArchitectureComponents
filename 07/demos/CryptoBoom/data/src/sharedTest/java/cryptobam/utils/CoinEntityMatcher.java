package cryptobam.utils;

import com.cryptoboom.data.entities.CryptoCoinEntity;

import org.mockito.ArgumentMatcher;

import java.util.List;

/**
 * Created by omrierez on 28.01.18.
 */
public class CoinEntityMatcher implements ArgumentMatcher<List<CryptoCoinEntity>> {
    private List<CryptoCoinEntity> coins;

    public CoinEntityMatcher(List<CryptoCoinEntity> coins) {
        this.coins = coins;
    }

    @Override
    public boolean matches(List<CryptoCoinEntity> coins) {
        if (coins.size() == 0)
            return true;
        final int size = this.coins.size();
        for (int i = 0; i < size; i++)
            if (!(this.coins.get(i).getId()
                    .compareTo(coins.get(i).getId()) == 0))
                return false;
        return true;
    }

}
