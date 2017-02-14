package auto.service;


import auto.model.Stock;
import auto.model.Strategy;
import com.ib.client.EWrapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Fang on 2/10/2016.
 */
public interface AutoService extends EWrapper {

    void saveStocks(HashMap<Integer, Stock> m_mapStock);

    void saveStrategies(HashMap<Integer, Strategy> m_mapStrategy);

    ArrayList<Strategy> loadStrategies();
}
