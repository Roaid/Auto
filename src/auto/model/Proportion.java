package auto.model;

import auto.model.Stock;
import auto.model.Strategy;
import com.ib.client.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@Entity
@Table(name = "Proportion")
public class Proportion implements Strategy {
    @Id
    private String code;
    @ManyToOne()
    @Cascade(value = org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "stock1")
    private Stock stock1;
    @ManyToOne()
    @Cascade(value = org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "stock2")
    private Stock stock2;
    @Transient
    private Contract c_contract = new Contract();    //Combo contract
    @Transient
    private Order order = new Order();

    @Transient
    private Timer timer;

    private int order_id;
    private int volume1, volume2;
    private int volume1_change, volume2_change;
    private double threshold;
    @Transient
    private int status;  //0 stop, 1 waiting, 2 PendingSubmit, 3 PreSubmitted or Submitted, 4 Cancelled,5 Filled
    private double remain;
    private double last_remain;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    public Stock getStock1() {
        return stock1;
    }

    public Stock getStock2() {
        return stock2;
    }

    public void setStock1(Stock stock1) {
        this.stock1 = stock1;
    }

    public void setStock2(Stock stock2) {
        this.stock2 = stock2;
    }

    @Override
    public Contract getC_contract() {
        return c_contract;
    }

    @Override
    public void setC_contract(Contract c_contract) {
        this.c_contract = c_contract;
    }

    @Override
    public Order getOrder() {
        return order;
    }

    @Override
    public void setOrder(Order order) {
        this.order = order;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public int getOrder_id() {
        return order_id;
    }

    @Override
    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public int getVolume1() {
        return volume1;
    }

    public int getVolume2() {
        return volume2;
    }

    public void setVolume1(int volume1) {
        this.volume1 = volume1;
    }

    public void setVolume2(int volume2) {
        this.volume2 = volume2;
    }

    public int getVolume1_change() {
        return volume1_change;
    }

    public int getVolume2_change() {
        return volume2_change;
    }

    public void setVolume1_change(int volume1_change) {
        this.volume1_change = volume1_change;
    }

    public void setVolume2_change(int volume2_change) {
        this.volume2_change = volume2_change;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    public Proportion() {
        threshold = 0.01;
        status = 0;
        order_id = -1;
    }


    @Override
    public boolean compare() {
        //test data
        /*stock2.setBpirce(124); //IYW
        stock2.setApirce(124.5);
        stock1.setBpirce(80); //XLV
        stock1.setApirce(81);*/
        //test data
        double sum = volume1 * (stock1.getBpirce() + stock1.getApirce()) / 2 + volume2 * (stock2.getBpirce() +
                stock2.getApirce()) / 2;
        double sum1 = volume1 * stock1.getBpirce() - volume2 * stock2.getApirce();
        if (sum1 > 0 && (sum1 / sum) > threshold) {
            System.out.println("way 1 \n");
            //Create a contract
            c_contract.symbol(code);
            c_contract.secType("BAG");
            c_contract.currency("USD");
            c_contract.exchange("SMART");

            //Calculate size of each leg.
            int volume_leg1 = (int) ((sum1 / 2) / stock1.getBpirce());
            int volume_leg2 = (int) ((volume_leg1 * stock1.getBpirce() + remain) / stock2.getApirce());
            int multiple = maxCommonDivisor(volume_leg1, volume_leg2);
            volume1_change = -volume_leg1;
            volume2_change = volume_leg2;
            System.out.println("multiple: " + multiple);
            System.out.println("leg1: " + (volume_leg1 / multiple));
            System.out.println("leg2: " + (volume_leg2 / multiple));
            System.out.println(stock1.getCode() + "->" + volume1 + "->" + stock1.getApirce() + "->" + volume_leg1);
            System.out.println(stock2.getCode() + "->" + volume2 + "->" + stock2.getApirce() + "->" + volume_leg2);

            last_remain = remain;
            remain = (volume_leg1 * stock1.getBpirce() + remain) - (volume_leg2 * stock2.getApirce());

            //Create legs.
            ComboLeg leg1 = new ComboLeg();
            ComboLeg leg2 = new ComboLeg();
            ArrayList<ComboLeg> addAllLegs = new ArrayList<ComboLeg>();
            leg1.conid(stock1.getContract().conid());
            leg1.ratio(volume_leg1 / multiple);
            leg1.action("SELL");
            leg1.exchange("SMART");
            addAllLegs.add(leg1);
            leg2.conid(stock2.getContract().conid());
            leg2.ratio(volume_leg2 / multiple);
            leg2.action("BUY");
            leg2.exchange("SMART");
            addAllLegs.add(leg2);
            c_contract.comboLegs(addAllLegs);
            //Create an order
            order.account("DU598622");
            order.orderType("LMT");
            order.totalQuantity(multiple);
            //order.tif("GTC");
            //order.goodTillDate("20170116 21:43:04 EST");
            order.orderComboLegs(new ArrayList<OrderComboLeg>());

            OrderComboLeg comboLeg1 = new OrderComboLeg();
            comboLeg1.price(stock1.getBpirce());
            order.orderComboLegs().add(comboLeg1);

            OrderComboLeg comboLeg2 = new OrderComboLeg();
            comboLeg2.price(stock2.getApirce());
            order.orderComboLegs().add(comboLeg2);

            List<TagValue> smartComboRoutingParams = new ArrayList<TagValue>();
            smartComboRoutingParams.add(new TagValue("NonGuaranteed", "1"));
            order.smartComboRoutingParams((ArrayList<TagValue>) smartComboRoutingParams);
            return true;
        }
        double sum2 = volume2 * stock2.getBpirce() - volume1 * stock1.getApirce();
        if (sum2 > 0 && (sum2 / sum) > threshold) {
            System.out.println("way 2");
            //Create a contract
            c_contract.symbol(code);
            c_contract.secType("BAG");
            c_contract.currency("USD");
            c_contract.exchange("SMART");

            //Calculate size of each leg.
            int volume_leg1 = (int) ((sum2 / 2) / stock2.getBpirce());
            int volume_leg2 = (int) ((volume_leg1 * stock2.getBpirce() + remain) / stock1.getApirce());
            int multiple = maxCommonDivisor(volume_leg1, volume_leg2);
            volume1_change = volume_leg2;
            volume2_change = -volume_leg1;
            System.out.println("multiple: " + multiple);
            System.out.println("leg1: " + (volume_leg1 / multiple));
            System.out.println("leg2: " + (volume_leg2 / multiple));
            System.out.println(stock2.getCode() + "->" + volume2 + "->" + stock2.getApirce() + "->" + volume_leg1);
            System.out.println(stock1.getCode() + "->" + volume1 + "->" + stock1.getApirce() + "->" + volume_leg2);


            last_remain = remain;
            remain = (volume_leg1 * stock2.getBpirce() + remain) - (volume_leg2 * stock1.getApirce());

            //Create legs.
            ComboLeg leg1 = new ComboLeg();
            ComboLeg leg2 = new ComboLeg();
            ArrayList<ComboLeg> addAllLegs = new ArrayList<ComboLeg>();
            leg1.conid(stock1.getContract().conid());
            leg1.ratio(volume_leg2 / multiple);
            leg1.action("BUY");
            leg1.exchange("SMART");
            leg2.conid(stock2.getContract().conid());
            leg2.ratio(volume_leg1 / multiple);
            leg2.action("SELL");
            leg2.exchange("SMART");
            addAllLegs.add(leg1);
            addAllLegs.add(leg2);
            c_contract.comboLegs(addAllLegs);
            //Create an order
            order.account("DU598622");
            order.orderType("LMT");
            order.totalQuantity(multiple);
            //order.tif("GTC");
            //order.goodTillDate("20170116 21:43:04 EST");
            order.orderComboLegs(new ArrayList<OrderComboLeg>());

            OrderComboLeg comboLeg1 = new OrderComboLeg();
            //comboLeg.price(stock1.getBpirce());
            comboLeg1.price(stock1.getApirce());
            order.orderComboLegs().add(comboLeg1);
            OrderComboLeg comboLeg2 = new OrderComboLeg();
            comboLeg2.price(stock2.getBpirce());
            order.orderComboLegs().add(comboLeg2);

            List<TagValue> smartComboRoutingParams = new ArrayList<TagValue>();
            smartComboRoutingParams.add(new TagValue("NonGuaranteed", "1"));
            order.smartComboRoutingParams((ArrayList<TagValue>) smartComboRoutingParams);
            return true;
        }
        return false;
    }

    @Override
    public void fillorder() {
        volume1 = volume1 + volume1_change;
        volume2 = volume2 + volume2_change;
        volume1_change = 0;
        volume2_change = 0;
    }


    private int maxCommonDivisor(int m, int n) {
        if (m < n) {
            int temp = m;
            m = n;
            n = temp;
        }
        while (m % n != 0) {
            int temp = m % n;
            m = n;
            n = temp;
        }
        return n;
    }
}