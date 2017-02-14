package auto.model;

import com.ib.client.Contract;
import org.hibernate.annotations.Cascade;


import javax.persistence.*;

@Entity
@Table(name = "Stock")
public class Stock implements Comparable<Stock> {
    @Id
    private String code;
    @OneToOne()
    @Cascade(value = org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "contract")
    private Contract contract;
    private double bprice;
    private double aprice;

    public Stock(String instring) {
        code = instring;
        bprice = -1;
        aprice = -1;
    }

    public Stock() {
        bprice = -1;
        aprice = -1;
    }

    public void setCode(String instring) {
        this.code = instring;
    }

    public String getCode() {
        return code;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public void setBpirce(double indouble) {
        bprice = indouble;
    }

    public double getBpirce() {
        return bprice;
    }

    public void setApirce(double indouble) {
        aprice = indouble;
    }

    public double getApirce() {
        return aprice;
    }

    @Override
    public int compareTo(Stock o) {
        if (code.equals(o.getCode())) {
            return 0;
        } else {
            return -1;
        }
    }

    public int compareTo(String in) {
        if (code.toUpperCase().equals(in.toUpperCase())) {
            return 0;
        } else {
            return -1;
        }
    }

    public int compareTo(Contract m_contract) {
        if (m_contract.equals(contract)) {
            return 0;
        } else {
            return -1;
        }
    }
}