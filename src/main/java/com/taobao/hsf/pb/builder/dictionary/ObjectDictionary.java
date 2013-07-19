package com.taobao.hsf.pb.builder.dictionary;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.taobao.hsf.pb.builder.context.DescriptorGenerateContext;
import com.taobao.hsf.pb.builder.impl.ArrayBuilder;
import com.taobao.hsf.pb.builder.impl.ByteBuilder;
import com.taobao.hsf.pb.builder.impl.CharBuilder;
import com.taobao.hsf.pb.builder.impl.CollectionBuilder;
import com.taobao.hsf.pb.builder.impl.DefaultBuilder;
import com.taobao.hsf.pb.builder.impl.EnumBuilder;
import com.taobao.hsf.pb.builder.impl.JavaBeanBuilder;
import com.taobao.hsf.pb.builder.impl.MapBuilder;
import com.taobao.hsf.pb.builder.impl.ShortBuilder;
import com.taobao.hsf.pb.builder.impl.biz.ForestReadStdCategoryPropertyDOBuilder;
import com.taobao.hsf.pb.builder.spi.Builder;
import com.taobao.hsf.pb.builder.spi.DescriptorBuilder;

public class ObjectDictionary
{
	static private final Log LOGGER = LogFactory.getLog("com.taobao.hsf");
	private final ConcurrentMap<String, Builder> builders;

	public ObjectDictionary()
	{
		builders = new ConcurrentHashMap<String, Builder>();

		addBuilder(int.class, new DefaultBuilder(int.class));
		addBuilder(Integer.class, new DefaultBuilder(Integer.class));
		addBuilder(boolean.class, new DefaultBuilder(boolean.class));
		addBuilder(Boolean.class, new DefaultBuilder(Boolean.class));
		addBuilder(short.class, new ShortBuilder(short.class));
		addBuilder(Short.class, new ShortBuilder(short.class));
		addBuilder(byte.class, new ByteBuilder(byte.class));
		addBuilder(Byte.class, new ByteBuilder(Byte.class));
		addBuilder(long.class, new DefaultBuilder(long.class));
		addBuilder(Long.class, new DefaultBuilder(Long.class));
		addBuilder(float.class, new DefaultBuilder(float.class));
		addBuilder(Float.class, new DefaultBuilder(Float.class));
		addBuilder(double.class, new DefaultBuilder(double.class));
		addBuilder(Double.class, new DefaultBuilder(Double.class));
		addBuilder(String.class, new DefaultBuilder(String.class));
		addBuilder(char.class, new CharBuilder(char.class));
		addBuilder(Character.class, new CharBuilder(Character.class));
		
		init();
	}
	
	public void init()
	{
		this.addBuilder("com.taobao.forest.domain.dataobject.std.read.StdCategoryPropertyDO", new ForestReadStdCategoryPropertyDOBuilder(null));
	}

	public void addBuilder(final java.lang.reflect.Type type, final Builder builder)
	{
		builders.put(type.toString(), builder);
	}
	
	public void addBuilder(final String typenName, final Builder builder)
	{
		builders.put(typenName, builder);
	}

	public DescriptorBuilder getDescriptorBuilder(final Class<?> clazzType)
	{
		return getBuilder(clazzType);
	}

	public Builder getBuilder(final java.lang.reflect.Type clazzType)
	{
		Builder builder = builders.get(clazzType.toString());
		if (builder == null)
		{
			Builder freshBuilder = null;
			try
			{
				freshBuilder = createBuilder(clazzType);
			} catch (Exception e)
			{
				LOGGER.error(e);
			}
			if (freshBuilder == null)
			{
				freshBuilder = NULL_BUILDER;
			}
			builder = builders.putIfAbsent(clazzType.toString(), freshBuilder);
			if (builder == null)
			{
				builder = freshBuilder;
			}
		}
		return (builder == NULL_BUILDER) ? null : builder;
	}

	private Builder createBuilder(final java.lang.reflect.Type clazzType) throws Exception
	{
		if (clazzType instanceof ParameterizedType)
		{
			ParameterizedType parameterizedType = (ParameterizedType) clazzType;
			Class rawType = (Class) parameterizedType.getRawType();
			if (Collection.class.isAssignableFrom(rawType))
			{
				if (parameterizedType.getActualTypeArguments().length != 1)
				{
					throw new Exception("未曾预料到的集合泛型参数个数:" + parameterizedType.getActualTypeArguments().length);
				}
				return new CollectionBuilder(rawType, (Class) parameterizedType.getActualTypeArguments()[0]);
			} else if (Map.class.isAssignableFrom(rawType))
			{
				if (parameterizedType.getActualTypeArguments().length != 2)
				{
					throw new Exception("未曾预料到的MAP泛型参数个数:" + parameterizedType.getActualTypeArguments().length);
				}
				return new MapBuilder(rawType, (Class) parameterizedType.getActualTypeArguments()[0], (Class) parameterizedType.getActualTypeArguments()[1]);
			}

		} else if ((clazzType instanceof Class) && (((Class) clazzType)).isArray())
		{
			return new ArrayBuilder(((Class) clazzType).getComponentType());

		} else if (clazzType instanceof GenericArrayType)
		{
			return new ArrayBuilder((Class) (((GenericArrayType) clazzType).getGenericComponentType()));
		} else if (Enum.class.isAssignableFrom((Class<?>) clazzType))
		{
			return new EnumBuilder((Class) clazzType);
		} else
		{
			if (clazzType.getClass().getName().equals("sun.reflect.generics.reflectiveObjects.TypeVariableImpl"))
			{
				LOGGER.error("遇到不能解析的类对象!");
				return null;
			}

			return JavaBeanBuilder.create((Class) clazzType);
		}

		LOGGER.error("未能找到到该类型的builder:" + clazzType);
		return null;
	}

	// ==============================================================================================================
	private static final Builder NULL_BUILDER = new Builder()
	{
		public Message buildMessage(Descriptor descriptor, Object object)
		{
			return null;
		}

		public void buildMessageField(com.google.protobuf.Message.Builder dynamicBuilder, FieldDescriptor fieldDescritptor, Object value)
		{
			
		}

		public Object buildObject(Object message)
		{
			return null;
		}

		public Descriptor buildDescriptor(DescriptorGenerateContext context, Class Clazz)
		{
			return null;
		}


	};


}
