package com.taobao.hsf.pb.builder.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.taobao.hsf.pb.builder.context.BuilderContext;
import com.taobao.hsf.pb.builder.context.DescriptorGenerateContext;
import com.taobao.hsf.pb.builder.context.DeserializationContext;
import com.taobao.hsf.pb.builder.context.SerializationContext;
import com.taobao.hsf.pb.builder.spi.Builder;

public class MapBuilder extends NullDescriptorBuilder implements Builder
{

	private Class mapClass;
	private Class keyClass;
	private Class dataClass;
	private Builder keyBuilder;
	private Builder dataBuilder;
	

	public MapBuilder(final Class mapClass, final Class keyClass, final Class dataClass)
	{

		this.mapClass = mapClass;
		this.keyClass = keyClass;
		this.dataClass = dataClass;
		this.keyBuilder = BuilderContext.getContext().getObjectDictionary().getBuilder(keyClass);
		this.dataBuilder = BuilderContext.getContext().getObjectDictionary().getBuilder(dataClass);
	}

	public Message buildMessage(Descriptor descriptor, Object object)
	{
		return null;
	}

	public Object buildObject(Object messageCollection)
	{
		Collection<Message> list = (Collection<Message>) messageCollection;
		Map map;
		try
		{
			if (this.mapClass.equals(Map.class))
			{
				map = new HashMap();
			} else
			{
				map = (Map) this.mapClass.newInstance();
			}
		} catch (InstantiationException e)
		{
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}

		for (Message message : list)
		{
			Object keyMessage = message.getField(message.getDescriptorForType().findFieldByName("key"));
			Object dataMessage = message.getField(message.getDescriptorForType().findFieldByName("data"));
			Object key = keyBuilder.buildObject(keyMessage);
			Object data = dataBuilder.buildObject(dataMessage);
			map.put(key, data);
		}
		return map;
	}

	public void buildMessageField(Message.Builder dynamicBuilder, FieldDescriptor fieldDescritptor, Object value)
	{
		Map map = (Map) value;
		Descriptor descriptor = dynamicBuilder.getDescriptorForType().findNestedTypeByName("MapEntry_" + fieldDescritptor.getName());
		for (Object object : map.entrySet())
		{
			DynamicMessage.Builder mapMesageBuilder = DynamicMessage.newBuilder(descriptor);
			Entry entry = (Entry) object;
			Object key = entry.getKey();
			Object data = entry.getValue();
			DescriptorProtos.FieldDescriptorProto.Type keyType = BuilderContext.getContext().getTypeDictionary().getByJavaType(key.getClass());
			DescriptorProtos.FieldDescriptorProto.Type dataType = BuilderContext.getContext().getTypeDictionary().getByJavaType(data.getClass());

			if (keyType.equals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE))
			{
				BuilderContext.getContext().getSerializationContext().fieldToMessage(mapMesageBuilder, descriptor.findFieldByName("key"),  BuilderContext.getContext().getSerializationContext().objectToSbMessage(key));
			} else
			{
				BuilderContext.getContext().getSerializationContext().fieldToMessage(mapMesageBuilder, descriptor.findFieldByName("key"), key);
			}

			if (dataType.equals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE))
			{
				BuilderContext.getContext().getSerializationContext().fieldToMessage(mapMesageBuilder, descriptor.findFieldByName("data"), BuilderContext.getContext().getSerializationContext().objectToSbMessage(data));
			} else
			{
				BuilderContext.getContext().getSerializationContext().fieldToMessage(mapMesageBuilder, descriptor.findFieldByName("data"), data);
			}

			dynamicBuilder.addRepeatedField(fieldDescritptor, mapMesageBuilder.build());
		}
	}

}
