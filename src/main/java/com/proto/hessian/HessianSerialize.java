package com.proto.hessian;

import com.caucho.hessian.io.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: server
 * Date: 13-7-18
 * Time: 下午3:18
 */
public class HessianSerialize {
    public static byte[] h1serialize(Object obj) throws IOException {
        if(obj==null) throw new NullPointerException();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(os);
        ho.writeObject(obj);
        ho.flush();
        ho.close();
        return os.toByteArray();
    }

    public static Object h1deserialize(byte[] by) throws IOException{
        if(by==null) throw new NullPointerException();

        ByteArrayInputStream is = new ByteArrayInputStream(by);
        HessianInput hi = new HessianInput(is);
        Object object = hi.readObject();
        hi.close();

        return object;
    }

    public static byte[] h2serialize(Object obj) throws IOException {
        if(obj==null) throw new NullPointerException();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output ho = new Hessian2Output(os);
        ho.writeObject(obj);
        ho.flush();
        ho.close();
        return os.toByteArray();
    }

    public static Object h2deserialize(byte[] by) throws IOException{
        if(by==null) throw new NullPointerException();

        ByteArrayInputStream is = new ByteArrayInputStream(by);
        Hessian2Input hi = new Hessian2Input(is);
        Object object = hi.readObject();
        hi.close();

        return object;
    }

    public static byte[] h2serializeDeflate(Object obj) throws IOException {
        if(obj==null) throw new NullPointerException();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output ho = new Hessian2Output(os);
        ho = new Deflation().wrap(ho);
        ho.writeObject(obj);
        ho.flush();
        ho.close();
        return os.toByteArray();
    }

    public static Object h2deserializeDeflate(byte[] by) throws IOException{
        if(by==null) throw new NullPointerException();

        ByteArrayInputStream is = new ByteArrayInputStream(by);
        Hessian2Input hi = new Hessian2Input(is);
        hi = new Deflation().unwrap(hi);
        Object object = hi.readObject();
        hi.close();

        return object;
    }
}

