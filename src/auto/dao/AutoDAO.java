package auto.dao;


import auto.model.Stock;
import auto.model.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Fang on 25/1/2017.
 */
public interface AutoDAO {

    List<Stock> getAllStocksData(String wherePart, String orderByPart, String rowNumPart);

    int getCurrentStocksCount(String wherePart);

    int setAllStocksData();

    void saveAStock(Stock stock);

    void saveStocks(HashMap<Integer, Stock> m_mapStock);

    void saveStrategies(HashMap<Integer, Strategy> m_mapStrategy);

    ArrayList<Strategy> loadStrategies();
}
