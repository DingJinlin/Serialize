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
        beanChild.setNumber1(1);
        beanChild.setNumber2(2);

        bean = new Bean();
        bean.setName("bean");
        bean.setArg(1);

        bean.setBeanChild(beanChild);
        Map<String, Integer> beanChildMap = new HashMap<String, Integer>();
        for(int i = 0; i != 10; i++) {
            beanChildMap.put(String.valueOf(i), i);
        }
        bean.setBeanChildMap(beanChildMap);

        List<BeanChild> beanChildList = new ArrayList<BeanChild>();
        for(int i = 0; i != 10; i++) {
            beanChildList.add(beanChild);
        }
        bean.setBeanChildList(beanChildList);



    }

    public int testHessian1Serialize() {
        byte[] data = new byte[0];
        try {
            data = HessianSerialize.h1serialize(bean);
            HessianSerialize.h1deserialize(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data.length;
    }

    public int testHessian2Serialize() {
        byte[] data = new byte[0];
        try {
            data = HessianSerialize.h2serialize(bean);
            HessianSerialize.h2deserialize(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data.length;
    }

    public int testHessian2DefSerialize() {
        byte[] data = new byte[0];
        try {
            data = HessianSerialize.h2serializeDeflate(bean);
            HessianSerialize.h2deserializeDeflate(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data.length;
    }

    public int testProbuf2Pojo() {
        byte[] data = new byte[0];
        try {
            data = ProtobufSerialize.coding(bean.getClass(), bean);
            ProtobufSerialize.decoding(bean.getClass(), data);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return data.length;
    }

    @Test
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

            data = ProtobufSerialize.coding(bean.getClass(), bean);
            System.out.println(new String(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

