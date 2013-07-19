package com.taobao.hsf.pb.builder.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Message;
import com.taobao.hsf.pb.builder.context.BuilderContext;
import com.taobao.hsf.pb.builder.spi.Builder;

public class ArrayBuilder extends NullDescriptorBuilder implements Builder
{

	private Class clazz;
	private Builder elementBuilder;

	public ArrayBuilder(Class clazz)
	{
		this.clazz = clazz;
		this.elementBuilder = BuilderContext.getContext().getObjectDictionary().getBuilder(clazz);
	}

	public Message buildMessage(Descriptor descriptor, Object object)
	{
		return null;
	}

	public Object buildObject(Object message)
	{

		Collection list = (Collection) message;
		int length = list.size();
		Object array = Array.newInstance((Class<?>) clazz, length);
		Iterator iterator = list.iterator();
		for (int i = 0; i < length; i++)
		{
			Object object = iterator.next();
			Array.set(array, i, elementBuilder.buildObject(object));
		}

		return array;
	}

	public void buildMessageField(Message.Builder dynamicBuilder, FieldDescriptor fieldDescritptor, Object value)
	{
		int length = Array.getLength(value);
		for (int i = 0; i < length; i++)
		{
			Object object = Array.get(value, i);
			com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type type = BuilderContext.getContext().getTypeDictionary().getByJavaType(object.getClass());

			if (type.equals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE))
			{
				dynamicBuilder.addRepeatedField(fieldDescritptor, BuilderContext.getContext().getSerializationContext().objectToSbMessage(object));
			} else
			{
				dynamicBuilder.addRepeatedField(fieldDescritptor, object);
			}
		}
	}
}
