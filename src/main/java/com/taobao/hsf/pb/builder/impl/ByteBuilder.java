package com.taobao.hsf.pb.builder.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.taobao.hsf.pb.builder.context.DescriptorGenerateContext;
import com.taobao.hsf.pb.builder.context.DeserializationContext;
import com.taobao.hsf.pb.builder.context.SerializationContext;
import com.taobao.hsf.pb.builder.spi.Builder;

public class ByteBuilder extends NullDescriptorBuilder implements Builder
{

	public Class clazz;

	public ByteBuilder(Class clazz)
	{
		this.clazz = clazz;
	}

	public Message buildMessage(Descriptor descriptor, Object object)
	{
		return null;
	}

	public void buildMessageField(Message.Builder dynamicBuilder, FieldDescriptor fieldDescritptor, Object value)
	{
		final byte b[] = new byte[]
		{ (Byte) value };
		ByteString byteString = ByteString.copyFrom(b);
		dynamicBuilder.setField(fieldDescritptor, byteString);
	}

	public Byte buildObject(Object message)
	{
		return null;
	}

}
