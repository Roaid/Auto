package auto.dao;

import auto.dao.util.HibernateUtil;
import auto.model.Stock;
import auto.model.Strategy;
import auto.model.StrategyImpl;
import com.ib.client.Contract;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import java.util.*;

/**
 * Created by Fang on 2017/1/25.
 */
@Repository
public class AutoDAOImpl implements AutoDAO {

    @Resource(name="SessionFactory")
    private SessionFactory sessionFactory;

    @Override
    public List<Stock> getAllStocksData(String wherePart, String orderByPart, String rowNumPart) {
        return null;
    }

    @Override
    public int getCurrentStocksCount(String wherePart) {
        return 0;
    }

    @Override
    public int setAllStocksData() {
        return 0;
    }

    @Override
    public void saveAStock(Stock stock) {
        Stock test = new Stock("IBM");
        Contract tmp = new Contract();
        tmp.conid(908621);
        test.setContract(tmp);
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        session.save(test);
        session.getTransaction().commit();
    }

    @Override
    public void saveStocks(HashMap<Integer, Stock> m_mapStock) {
        saveAnything(m_mapStock);
    }


    @Override
    public void saveStrategies(HashMap<Integer, StrategyImpl> m_mapStrategy) {
        saveAnything(m_mapStrategy);
    }

    @Override
    public ArrayList<Strategy> loadStrategies() {
        ArrayList<Strategy> tmp = null;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            String hql = "from Proportion";
            Query query = session.createQuery(hql);
            tmp = (ArrayList<Strategy>) query.list();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            throw e;  //throw the error message.
        } finally {
            session.close();
        }
        return tmp;
    }
    @Override
    public void saveAnything(HashMap<Integer, ?> m_mapAnything) {
        Session session = null;
        Transaction tx = null;
        Iterator<? extends Map.Entry<Integer, ?>> iterator = m_mapAnything.entrySet().iterator();
        try {
            session = sessionFactory.getCurrentSession();
            tx = session.beginTransaction();
            while (iterator.hasNext()) {
                HashMap.Entry<Integer, ?> entry = iterator.next();
                session.saveOrUpdate(entry.getValue());
            }
            session.getTransaction().commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            throw e;  //throw the error message.
        } finally {
            session.close();
        }
    }


}
