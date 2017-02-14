package auto.model;

import com.ib.client.Contract;
import com.ib.client.Order;

import javax.persistence.Entity;


public interface Strategy {
    String getCode();

    void setCode(String code);

    int getStatus();

    void setStatus(int status);

    Contract getC_contract();

    void setC_contract(Contract c_contract);

    Order getOrder();

    void setOrder(Order order);

    int getOrder_id();

    void setOrder_id(int order_id);

    boolean compare();

    void fillorder();
}