package com.proto.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.taobao.hsf.pb.builder.context.BuilderContext;
import com.taobao.hsf.pb.builder.impl.JavaBeanBuilder;

/**
 * User: server
 * Date: 13-7-19
 * Time: 上午9:41
 */
public class ProtobufSerialize {
    public static byte[] coding(Class classz, Object object) {
        BuilderContext builderContext = BuilderContext.getContext();
        Descriptors.Descriptor descriptor = builderContext.descriptorFromClass(classz);

        JavaBeanBuilder builder = JavaBeanBuilder.create(classz);
        byte[] data = builder.buildMessage(descriptor, object).toByteArray();

        return data;
    }

    public static Object decoding(Class classz, byte[] data) throws InvalidProtocolBufferException {
        BuilderContext builderContext = BuilderContext.getContext();
        Descriptors.Descriptor descriptor = builderContext.descriptorFromClass(classz);
        DynamicMessage message = DynamicMessage.parseFrom(descriptor, data);

        JavaBeanBuilder builder = JavaBeanBuilder.create(classz);
        Object object = builder.buildObject(message);

        return object;
    }
}
