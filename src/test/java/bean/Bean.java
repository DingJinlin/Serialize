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
    String name;
    int arg;

    List<BeanChild> beanChildList;
    Map<String, Integer> beanChildMap;
    BeanChild beanChild;

    public Bean() {

    }

    @SuppressWarnings("unchecked")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public int getArg() {
        return arg;
    }

    public void setArg(int arg) {
        this.arg = arg;
    }

    @SuppressWarnings("unchecked")
    public List<BeanChild> getBeanChildList() {
        return beanChildList;
    }

    public void setBeanChildList(List<BeanChild> beanChildList) {
        this.beanChildList = beanChildList;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> getBeanChildMap() {
        return beanChildMap;
    }

    public void setBeanChildMap(Map<String, Integer> beanChildMap) {
        this.beanChildMap = beanChildMap;
    }

    @SuppressWarnings("unchecked")
    public BeanChild getBeanChild() {
        return beanChild;
    }

    public void setBeanChild(BeanChild beanChild) {
        this.beanChild = beanChild;
    }
}
