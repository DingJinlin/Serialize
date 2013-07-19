package com.taobao.hsf.pb.builder.impl.biz;

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
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.taobao.hsf.pb.builder.context.DescriptorGenerateContext;
import com.taobao.hsf.pb.builder.impl.JavaBeanBuilder;

public class ForestReadStdCategoryPropertyDOBuilder extends JavaBeanBuilder
{
	static private final Log LOGGER = LogFactory.getLog("com.taobao.hsf");

	private volatile boolean inited = false;

	private synchronized void init()
	{
		if (inited)
			return;
		try
		{
			clazz = (Class) Class.forName("com.taobao.forest.domain.dataobject.std.impl.DefaultStdCategoryPropertyDO");

			PropertyDescriptor[] temp = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
			for (PropertyDescriptor propertyDescriptor : temp)
			{
				if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null)
				{
					properties.add(propertyDescriptor);
				}
			}

			propertyDescriptors = properties.toArray(new PropertyDescriptor[]
			{});
			this.clazz = clazz;
		} catch (ClassNotFoundException e)
		{
			LOGGER.error("创建ForestReadStdCategoryPropertyDOBuilder失败!", e);
			return;
		} catch (IntrospectionException e)
		{
			LOGGER.error("创建ForestReadStdCategoryPropertyDOBuilder失败!", e);
			return;
		}

		inited = true;
	}

	public ForestReadStdCategoryPropertyDOBuilder(Class clazz)
	{
	}

	public Descriptor buildDescriptor(DescriptorGenerateContext context, Class clazz)
	{

		init();
		String name = "com.taobao.forest.domain.dataobject.std.read.StdCategoryPropertyDO";
		String simpleName = "StdCategoryPropertyDO";
		String packageName = "com.taobao.forest.domain.dataobject.std.read";

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

}
