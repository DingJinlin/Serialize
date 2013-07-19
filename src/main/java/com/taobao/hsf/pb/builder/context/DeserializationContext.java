package com.taobao.hsf.pb.builder.context;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.taobao.hsf.pb.builder.spi.ObjectBuilder;

public class DeserializationContext
{
	private BuilderContext builderContext;

	public DeserializationContext(BuilderContext builderContext)
	{
		this.builderContext = builderContext;
	}

	public Object fieldValueToObject(final FieldDescriptor field)
	{
		return null;
	}

	public Object messageToObject(final java.lang.reflect.Type type, final Object message)
	{
		ObjectBuilder builder = builderContext.getObjectDictionary().getBuilder(type);
		return builder.buildObject(message);
	}
	
	
	public BuilderContext getBuilderContext()
	{
		return builderContext;
	}
}
