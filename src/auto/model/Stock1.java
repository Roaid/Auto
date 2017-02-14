package auto.model;

import com.ib.client.Contract;


import javax.persistence.*;


@Entity
@Table(name="Stock1")
public class Stock1 implements Comparable<Stock1> {
    @Id
	private String code;
    @OneToOne(cascade = { CascadeType.PERSIST ,CascadeType.ALL})
    @JoinColumn(name="contract")
    private Contract contract;
    private double bprice;
    private double aprice;
    public Stock1(){
    }
    Stock1(String code){
        this.code = code;
        this.bprice = 0;
        this.aprice = 0;
    }
	public void setCode(String code){
		this.code = code;
	}
	public String getCode(){
		return code;
	}
    public Contract getContract() {
        return contract;
    }
    public void setContract(Contract contract) {
        this.contract = contract;
    }
    public void setBprice(double bprice){
        this.bprice = bprice;
    }
    public double getBprice(){
        return bprice;
    }
    public void setAprice(double indouble){
        this.aprice = indouble;
    }
    public double getAprice(){
        return this.aprice;
    }
	@Override
	public int compareTo(Stock1 o) {
		if(code.equals(o.getCode())) {
			return 0;
		} else {
			return -1;
		}
	}

	public int compareTo(String in) {
		if(code.equals(in)) {
			return 0;
		} else {
			return -1;
		}
	}

	/*public int compareTo(Contract m_contract) {
		if(m_contract.equals(contract)) {
			return 0;
		} else {
			return -1;
		}
	}*/
}