import bean.Bean;
import bean.BeanChild;
import com.google.protobuf.InvalidProtocolBufferException;
import com.proto.hessian.HessianSerialize;
import com.proto.protobuf.ProtobufSerialize;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Ding
 * Date: 13-7-18
 * Time: ����11:58
 */
public class TestPerformance {
    Bean bean;
    public TestPerformance() {
        BeanChild beanChild = new BeanChild();
        beanChild.setC(1);
        beanChild.setD(2);

        bean = new Bean();
        bean.setA("bean");
        bean.setB(1);

        bean.setBeanChild(beanChild);
        Map<String, Integer> beanChildMap = new HashMap<String, Integer>();
        List<Integer> beanChildList = new ArrayList<Integer>();
        for(int i = 0; i != 10; i++) {
            beanChildMap.put(String.valueOf(i), i);
            beanChildList.add(i);
        }
        bean.setBeanChildList(beanChildList);
        bean.setBeanChildMap(beanChildMap);
    }

    public int testHessian1Serialize() {
        byte[] data = new byte[0];
        try {
            data = HessianSerialize.h1serialize(bean);
            Bean hessian1Bean = (Bean)HessianSerialize.h1deserialize(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data.length;
    }

    public int testHessian2Serialize() {
        byte[] data = new byte[0];
        try {
            data = HessianSerialize.h2serialize(bean);
            Bean hessian2Bean = (Bean)HessianSerialize.h2deserialize(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data.length;
    }

    public int testHessian2DefSerialize() {
        byte[] data = new byte[0];
        try {
            data = HessianSerialize.h2serializeDeflate(bean);
            Bean hessian2DeflateBean = (Bean)HessianSerialize.h2deserializeDeflate(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data.length;
    }

    public int testProbuf2Pojo() {
        byte[] data = new byte[0];
        try {
            data = ProtobufSerialize.coding(bean.getClass(), bean);
            Bean bean1 = (Bean)ProtobufSerialize.decoding(bean.getClass(), data);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return data.length;
    }

//    @Test
    public void testPerformance() {
        TestPerformance testPerformance = new TestPerformance();
        int testTime = 1000;
        long startTime;
        long endTime;
        int dataLen = 0;

         // 预启动
        for(int i = 0; i != testTime; i++) {
            testPerformance.testHessian1Serialize();
        }

        startTime = System.currentTimeMillis();
        for(int i = 0; i != testTime; i++) {
            dataLen = testPerformance.testHessian1Serialize();
        }
        endTime = System.currentTimeMillis();
        System.out.println("Test Hessian1 count: " + testTime + " use time = " + (endTime - startTime) + " millis");
        System.out.println("data length = " + dataLen);

        startTime = System.currentTimeMillis();
        for(int i = 0; i != testTime; i++) {
            dataLen = testPerformance.testHessian2Serialize();
        }
        endTime = System.currentTimeMillis();
        System.out.println("Test Hessian2 count: " + testTime + " use time = " + (endTime - startTime) + " millis");
        System.out.println("data length = " + dataLen);

        startTime = System.currentTimeMillis();
        for(int i = 0; i != testTime; i++) {
            dataLen = testPerformance.testProbuf2Pojo();
        }
        endTime = System.currentTimeMillis();
        System.out.println("Test Probuf2Pojo count: " + testTime + " use time = " + (endTime - startTime) + " millis");
        System.out.println("data length = " + dataLen);

        startTime = System.currentTimeMillis();
        for(int i = 0; i != testTime; i++) {
            dataLen = testPerformance.testHessian2DefSerialize();
        }
        endTime = System.currentTimeMillis();
        System.out.println("Test Hessian2Def count: " + testTime + " use time = " + (endTime - startTime) + " millis");
        System.out.println("data length = " + dataLen);
    }

    @Test
    public void testHessianByteData() {
        byte[] data;
        try {
            data = HessianSerialize.h1serialize(bean);
            System.out.println(new String(data));

            data = HessianSerialize.h2serialize(bean);
            System.out.println(new String(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

