package auto.model;

import javax.persistence.*;

/**
 * Created by Fang on 2017/2/19.
 */
@Entity
@Table(name = "Strategy")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class StrategyImpl implements Strategy {
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Id
    private String code;
}
