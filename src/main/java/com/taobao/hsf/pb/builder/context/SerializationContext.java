package com.taobao.hsf.pb.builder.context;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.taobao.hsf.pb.builder.spi.MessageBuilder;

public class SerializationContext
{

	private BuilderContext builderContext;

	public SerializationContext(BuilderContext builderContext)
	{
		this.builderContext = builderContext;
	}

	@SuppressWarnings({ "rawtypes"})
	public Message objectToSbMessage(final Class clazz, final Object object)
	{
		Descriptor descriptor = builderContext.descriptorFromClass(clazz);
		MessageBuilder builder = builderContext.getObjectDictionary().getBuilder(clazz);

		return builder.buildMessage( descriptor, object);
	}

	public Message objectToSbMessage(final Object object)
	{
		Descriptor descriptor = builderContext.descriptorFromClass(object.getClass());
		MessageBuilder builder = builderContext.getObjectDictionary().getBuilder(object.getClass());

		return builder.buildMessage(descriptor, object);
	}
	
	public BuilderContext getBuilderContext()
	{
		return builderContext;
	}

	public void setBuilderContext(BuilderContext builderContext)
	{
		this.builderContext = builderContext;
	}

	public void fieldToMessage(Message.Builder dynamicBuilder, FieldDescriptor fieldDescritptor, Object value)
	{
		if (value == null)
		{
			return;
		}
		
		java.lang.reflect.Type type = fieldDescritptor.toProto().getFieldType()!=null?fieldDescritptor.toProto().getFieldType():value.getClass();
		MessageBuilder builder = builderContext.getObjectDictionary().getBuilder(type);
		builder.buildMessageField(dynamicBuilder, fieldDescritptor, value);
	}
}
