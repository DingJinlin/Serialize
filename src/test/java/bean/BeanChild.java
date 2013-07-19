package bean;

import java.io.Serializable;

/**
 * User: server
 * Date: 13-7-18
 * Time: 下午3:33
 */
public class BeanChild implements Serializable {
    int c;
    int d;

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }
}
