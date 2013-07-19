package com.taobao.hsf.pb.builder.dictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;

public class TypeDictionary
{

	private final ConcurrentMap<Class<?>, DescriptorProtos.FieldDescriptorProto.Type> _typesByJavaType;
	private final Map<DescriptorProtos.FieldDescriptorProto.Type,com.google.protobuf.Descriptors.FieldDescriptor.Type> typeMap;

	/**
	 * Creates a new {@link TypeDictionary} configured with the default types
	 * from the Fudge specification. Also includes some standard secondary
	 * types.
	 */
	public TypeDictionary()
	{
		_typesByJavaType = new ConcurrentHashMap<Class<?>, DescriptorProtos.FieldDescriptorProto.Type>();
		typeMap = new HashMap<DescriptorProtos.FieldDescriptorProto.Type,com.google.protobuf.Descriptors.FieldDescriptor.Type>();
		
		typeMap.put(Type.TYPE_BOOL, com.google.protobuf.Descriptors.FieldDescriptor.Type.BOOL);
		typeMap.put(Type.TYPE_BYTES, com.google.protobuf.Descriptors.FieldDescriptor.Type.BYTES);
		typeMap.put(Type.TYPE_SINT32, com.google.protobuf.Descriptors.FieldDescriptor.Type.SINT32);
		typeMap.put(Type.TYPE_SINT64, com.google.protobuf.Descriptors.FieldDescriptor.Type.SINT64);
		typeMap.put(Type.TYPE_INT64, com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64);
		typeMap.put(Type.TYPE_FLOAT, com.google.protobuf.Descriptors.FieldDescriptor.Type.FLOAT);
		typeMap.put(Type.TYPE_DOUBLE, com.google.protobuf.Descriptors.FieldDescriptor.Type.DOUBLE);
		typeMap.put(Type.TYPE_STRING, com.google.protobuf.Descriptors.FieldDescriptor.Type.STRING);

		addType(Type.TYPE_BOOL, boolean.class, Boolean.class, Boolean.TYPE);
		addType(Type.TYPE_BYTES, byte[].class, Byte[].class, Byte.TYPE,byte.class);
		addType(Type.TYPE_SINT32, char.class);
		addType(Type.TYPE_SINT32, short.class, Short.class, Short.TYPE);
		addType(Type.TYPE_SINT32, int.class, Integer.class, Integer.TYPE);
		addType(Type.TYPE_SINT64, long.class,Long.class, Long.TYPE);
		addType(Type.TYPE_FLOAT, float.class, Float.class, Float.TYPE);
		addType(Type.TYPE_DOUBLE, double.class, Double.class, Double.TYPE);
		addType(Type.TYPE_STRING, String.class);
		addType(Type.TYPE_MESSAGE, Object.class);
	}

	public void addType(DescriptorProtos.FieldDescriptorProto.Type type, Class<?>... alternativeTypes)
	{
		if (type == null)
		{
			throw new NullPointerException("Must not provide a null FudgeFieldType to add.");
		}

		for (Class<?> alternativeType : alternativeTypes)
		{
			_typesByJavaType.put(alternativeType, type);
		}
	}

	public DescriptorProtos.FieldDescriptorProto.Type getByJavaType(final Class<?> javaType)
	{
		if (javaType == null)
		{
			return null;
		}

		DescriptorProtos.FieldDescriptorProto.Type fieldType = _typesByJavaType.get(javaType);
		if (fieldType != null)
		{
			return fieldType;
		}
		for (Class<?> i : javaType.getInterfaces())
		{
			fieldType = getByJavaType(i);
			if (fieldType != null)
			{
				return fieldType;
			}
		}

		// FIXME 考虑到特殊情况的处理
		// com.taobao.forest.domain.dataobject.std.read.StdCategoryPropertyDO
		if (javaType.getName().equals("com.taobao.forest.domain.dataobject.std.read.StdCategoryPropertyDO"))
		{
			return _typesByJavaType.get(Object.class);
		}

		return getByJavaType(javaType.getSuperclass());
	}

}
