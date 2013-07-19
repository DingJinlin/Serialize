package com.taobao.hsf.pb.builder.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.taobao.hsf.pb.builder.context.BuilderContext;
import com.taobao.hsf.pb.builder.context.DescriptorGenerateContext;
import com.taobao.hsf.pb.builder.context.DeserializationContext;
import com.taobao.hsf.pb.builder.context.SerializationContext;
import com.taobao.hsf.pb.builder.spi.Builder;

public class CollectionBuilder extends NullDescriptorBuilder implements Builder
{
	private Class collectionClazz;
	private Class elementClazz;
	private Builder elementBuilder;

	public CollectionBuilder(Class clazz, Class elementClazz)
	{
		this.collectionClazz = clazz;
		this.elementClazz = elementClazz;
		this.elementBuilder = BuilderContext.getContext().getObjectDictionary().getBuilder(elementClazz);
	}

	public CollectionBuilder()
	{
	}

	public Message buildMessage(Descriptor descriptor, Object object)
	{

		return null;
	}

	public void buildMessageField(Message.Builder dynamicBuilder, FieldDescriptor fieldDescritptor, Object value)
	{
		Collection collection = (Collection) value;

		for (Object object : collection)
		{
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

	public Collection buildObject(Object message)
	{
		Collection collection = null;
		try
		{
			if (collectionClazz.equals(List.class))
			{
				collection = new ArrayList();
			} else
			{
				collection = (Collection) collectionClazz.newInstance();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		Collection list = (Collection) message;

		for (Object object : list)
		{
			collection.add(elementBuilder.buildObject(object));
		}

		return collection;
	}

}
