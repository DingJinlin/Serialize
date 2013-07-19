package bean;

import java.io.Serializable;

/**
 * User: server
 * Date: 13-7-18
 * Time: 下午3:33
 */
public class BeanChild implements Serializable {
    int number1;
    int number2;

    @SuppressWarnings("unused")
    public int getNumber1() {
        return number1;
    }

    public void setNumber1(int number1) {
        this.number1 = number1;
    }

    @SuppressWarnings("unused")
    public int getNumber2() {
        return number2;
    }

    public void setNumber2(int number2) {
        this.number2 = number2;
    }
}
