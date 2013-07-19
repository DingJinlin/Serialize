package com.taobao.hsf.pb.builder.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.taobao.hsf.pb.builder.context.BuilderContext;
import com.taobao.hsf.pb.builder.context.DescriptorGenerateContext;
import com.taobao.hsf.pb.builder.spi.Builder;
import com.taobao.hsf.pb.exception.PBException;

public class JavaBeanBuilder implements Builder
{
	static private final Log LOGGER = LogFactory.getLog("com.taobao.hsf");
	protected PropertyDescriptor[] propertyDescriptors;
	protected Class clazz;
	
	protected JavaBeanBuilder()
	{
		
	}

	public JavaBeanBuilder(Class clazz) throws IntrospectionException
	{
		PropertyDescriptor[] temp = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
		List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
		for (PropertyDescriptor propertyDescriptor : temp)
		{
			if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null)
			{
				properties.add(propertyDescriptor);
			}
		}
		propertyDescriptors = properties.toArray(new PropertyDescriptor[] {});
		this.clazz = clazz;
	}

	public Message buildMessage(Descriptor descriptor, Object object)
	{
		DynamicMessage.Builder dynamicBuilder = DynamicMessage.newBuilder(descriptor);
		List<FieldDescriptor> fieldDescritptors = descriptor.getFields();
		Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
		{
			properties.put(propertyDescriptor.getName(), propertyDescriptor);
		}

		for (FieldDescriptor fieldDescritptor : fieldDescritptors)
		{
			String fieldName = fieldDescritptor.getName();
			PropertyDescriptor propertyDescriptor = null;
			try
			{
				propertyDescriptor = properties.get(fieldName);
				Object fieldValue = propertyDescriptor.getReadMethod().invoke(object);
				BuilderContext.getContext().getSerializationContext().fieldToMessage(dynamicBuilder, fieldDescritptor, fieldValue);
			} catch (Exception e)
			{
				LOGGER.error("构建PB消息:" + descriptor.getFullName() + "时,属性:" + fieldName + " 设置失败!", e);
			}
		}

		return dynamicBuilder.build();
	}

	public Object buildObject(Object message)
	{

		Map<Descriptors.FieldDescriptor, Object> map = ((Message) message).getAllFields();

		Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
		{
			properties.put(propertyDescriptor.getName(), propertyDescriptor);
		}

		Object object = null;
		try
		{
			object = clazz.newInstance();
			for (Entry<Descriptors.FieldDescriptor, Object> entry : map.entrySet())
			{
				Descriptors.FieldDescriptor fieldDescritptor = entry.getKey();
				Object value = entry.getValue();
				java.lang.reflect.Type javaType = fieldDescritptor.toProto().getFieldType();
				PropertyDescriptor propertyDescriptor = properties.get(fieldDescritptor.getName());
				Builder fieldBuilder = BuilderContext.getContext().getObjectDictionary().getBuilder(javaType);
				propertyDescriptor.getWriteMethod().invoke(object, fieldBuilder.buildObject(value));
			}

		} catch (Exception e)
		{
			LOGGER.error("通过PB消息构建java对象:" + clazz.getName() + "时，失败!", e);
		}

		return object;
	}

	public Descriptor buildDescriptor(DescriptorGenerateContext context, Class clazz)
	{

		String name = clazz.getName();
		String simpleName = clazz.getSimpleName();
		String packageName = clazz.getPackage().getName();

		DescriptorProto.Builder descriptorProtoBuilder = DescriptorProto.newBuilder();
		FileDescriptorProto.Builder fileDescriptorProtoBuilder = FileDescriptorProto.newBuilder().setName(name);

		descriptorProtoBuilder.setName(simpleName);
		fileDescriptorProtoBuilder.setPackage(packageName);

		List<FileDescriptor> dependencies = new ArrayList<FileDescriptor>();

		int length = propertyDescriptors.length;
		for (int i = 0; i < length; i++)
		{
			PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
			try
			{
				LOGGER.info("pb:为java POJO：" + name + " 类型为：" + propertyDescriptor.getPropertyType() + "的属性" + propertyDescriptor.getName() + "生成对应的PB描述!");
				context.fieldToFieldDescriptor(descriptorProtoBuilder, dependencies, i + 1, propertyDescriptor);
			} catch (Exception e)
			{
				LOGGER.error("通过java类构建PB描述失败！", e);
			}
		}

		Map<String, FileDescriptor> map = new HashMap<String, FileDescriptor>();

		for (FileDescriptor fileDescriptor : dependencies)
		{
			map.put(fileDescriptor.getName(), fileDescriptor);
		}

		FileDescriptor[] deps = new FileDescriptor[map.size()];
		int i = 0;
		for (Entry<String, FileDescriptor> entry : map.entrySet())
		{
			deps[i++] = entry.getValue();
			fileDescriptorProtoBuilder.addDependency(entry.getKey());
		}

		DescriptorProto descriptorProto = descriptorProtoBuilder.build();
		fileDescriptorProtoBuilder.addMessageType(descriptorProto);
		FileDescriptorProto fileDescriptorProto = fileDescriptorProtoBuilder.build();

		try
		{
			FileDescriptor fileDescritptor = FileDescriptor.buildFrom(fileDescriptorProto, deps);
			context.addFileDescritptor(fileDescritptor);

			return fileDescritptor.findMessageTypeByName(simpleName);
		} catch (DescriptorValidationException e)
		{
			LOGGER.error(e);
		}

		return null;

	}

	public static JavaBeanBuilder create(final Class clazz)
	{
		try
		{
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();

			return new JavaBeanBuilder(clazz);
		} catch (IntrospectionException e)
		{
			throw new PBException("获取Bean属性描述出错!", e);
		}
	}

	public void buildMessageField(Message.Builder dynamicBuilder, FieldDescriptor fieldDescritptor, Object value)
	{
		dynamicBuilder.setField(fieldDescritptor, buildMessage(fieldDescritptor.getMessageType(), value));
	}

}