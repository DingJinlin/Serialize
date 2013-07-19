package bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * User: server
 * Date: 13-7-18
 * Time: 下午3:33
 */
public class Bean implements Serializable {
    String a;
    int b;

    List<Integer> beanChildList;
    Map<String, Integer> beanChildMap;
    BeanChild beanChild;

    public Bean() {

    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public List<Integer> getBeanChildList() {
        return beanChildList;
    }

    public void setBeanChildList(List<Integer> beanChildList) {
        this.beanChildList = beanChildList;
    }

    public Map<String, Integer> getBeanChildMap() {
        return beanChildMap;
    }

    public void setBeanChildMap(Map<String, Integer> beanChildMap) {
        this.beanChildMap = beanChildMap;
    }

    public BeanChild getBeanChild() {
        return beanChild;
    }

    public void setBeanChild(BeanChild beanChild) {
        this.beanChild = beanChild;
    }
}
