package com.taobao.hsf.pb.builder.context;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.taobao.hsf.pb.builder.dictionary.ObjectDictionary;
import com.taobao.hsf.pb.builder.spi.DescriptorBuilder;
import com.taobao.hsf.pb.exception.PBException;

public class DescriptorGenerateContext
{
	static private final Log LOGGER = LogFactory.getLog("com.taobao.hsf");

	private BuilderContext builderContext;

	public DescriptorGenerateContext(BuilderContext builderContext)
	{
		this.builderContext = builderContext;
	}

	/**
	 * 通过指定的class获得对应的protocol buffer 的descriptor 暂未考虑嵌套类的情况
	 * 
	 * @param clazz
	 * @return
	 */
	public <T> Descriptor descriptorFromClass(Class<T> clazz)
	{
		String simpleName = clazz.getSimpleName();
		FileDescriptor root = builderContext.getRootFileDescriptor();
		Descriptor descriptor = root.findMessageTypeByName(simpleName);

		for (FileDescriptor fd : root.getDependencies())
		{
			if (descriptor != null)
			{
				break;
			}
			Descriptor temp = fd.findMessageTypeByName(simpleName);
			if (null != temp && temp.getFullName().equals(clazz.getName()))
			{
				descriptor = temp;
			}
		}

		if (descriptor != null)
		{
			return descriptor;
		}
		// FIXME 当a->b...->a 这种依赖情况时 返回一个模拟的a描述符
		if (builderContext.generatedClassSet.contains(clazz))
		{
			String name = clazz.getName();
			LOGGER.error("循环依赖问题class:" + name);
			DescriptorProto.Builder descriptorProtoBuilder = DescriptorProto.newBuilder();
			FileDescriptorProto.Builder fileDescriptorProtoBuilder = FileDescriptorProto.newBuilder().setName(name);

			descriptorProtoBuilder.setName(simpleName);
			fileDescriptorProtoBuilder.setPackage(clazz.getPackage().getName());

			DescriptorProto descriptorProto = descriptorProtoBuilder.build();
			fileDescriptorProtoBuilder.addMessageType(descriptorProto);
			FileDescriptorProto fileDescriptorProto = fileDescriptorProtoBuilder.build();

			FileDescriptor fileDescritptor;
			try
			{
				fileDescritptor = FileDescriptor.buildFrom(fileDescriptorProto, new FileDescriptor[]
				{});
				return fileDescritptor.findMessageTypeByName(clazz.getSimpleName());
			} catch (DescriptorValidationException e)
			{
				e.printStackTrace();
			}

		}

		ObjectDictionary objectDictionary = builderContext.getObjectDictionary();
		DescriptorBuilder descriptorBuilder = objectDictionary.getDescriptorBuilder(clazz);

		builderContext.generatedClassSet.add(clazz);

		descriptor = descriptorBuilder.buildDescriptor(this, clazz);
		return descriptor;
	}

	@SuppressWarnings("rawtypes")
	public ServiceDescriptor serviceFromInterface(Class clazz)
	{
		String name = clazz.getName();
		String simpleName = clazz.getSimpleName();

		ServiceDescriptorProto.Builder descriptorProtoBuilder = ServiceDescriptorProto.newBuilder();
		FileDescriptorProto.Builder fileDescriptorProtoBuilder = FileDescriptorProto.newBuilder().setName(name);

		Set<Descriptor> descritptorSet = new HashSet<Descriptor>();
		descriptorProtoBuilder.setName(simpleName);
		fileDescriptorProtoBuilder.setPackage(clazz.getPackage().getName());
		// 对付方法名相同 而参数不同的方法
		Map<String, Integer> methodNames = new HashMap<String, Integer>();
		for (Method method : clazz.getMethods())
		{
			LOGGER.info("为service:" + name + "的方法:" + method.getName() + "生成PB描述");
			MethodDescriptorProto.Builder methodProtoBuilder = MethodDescriptorProto.newBuilder();
			methodArgToInputTypeDescriptor(fileDescriptorProtoBuilder, descriptorProtoBuilder, methodProtoBuilder, descritptorSet, methodNames, method);

			methodReturToOutputTypeDescritptor(fileDescriptorProtoBuilder, descriptorProtoBuilder, methodProtoBuilder, descritptorSet, methodNames, method);
			Integer number = methodNames.get(method.getName());
			if (number==1)
			{
				methodProtoBuilder.setName(method.getName());
			} else
			{
				methodProtoBuilder.setName(method.getName() + (number - 1));
			}
			String service = clazz.getName() + "@" + method.getName();
			for (Class<?> type : method.getParameterTypes())
			{
				service += "@" + type.getName();
			}

			builderContext.methodAsService.put(service, methodProtoBuilder.getName());
			builderContext.serviceAsMethod.put(clazz.getName() + "@" + methodProtoBuilder.getName(), method.getName());
			descriptorProtoBuilder.addMethod(methodProtoBuilder);
		}

		List<FileDescriptor> depsList = new ArrayList<Descriptors.FileDescriptor>();

		Set<String> depProtos = new HashSet<String>();
		for (Descriptor entry : descritptorSet)
		{
			if (entry == null)
				continue;
			String temp = entry.getFullName();
			if (!depProtos.contains(temp))
			{
				fileDescriptorProtoBuilder.addDependency(entry.getFullName());
				depsList.add(entry.getFile());
				depProtos.add(temp);
			}
		}

		ServiceDescriptorProto descriptorProto = descriptorProtoBuilder.build();
		fileDescriptorProtoBuilder.addService(descriptorProto);
		FileDescriptorProto fileDescriptorProto = fileDescriptorProtoBuilder.build();

		try
		{
			FileDescriptor fileDescritptor = FileDescriptor.buildFrom(fileDescriptorProto, depsList.toArray(new FileDescriptor[]
			{}));
			addFileDescritptor(fileDescritptor);
			return fileDescritptor.findServiceByName(clazz.getSimpleName());
		} catch (DescriptorValidationException e)
		{
			LOGGER.error("接口："+clazz+" 生成PB服务失败", e);
		}

		return null;
	}

	private void methodArgToInputTypeDescriptor(FileDescriptorProto.Builder fileDescriptorProtoBuilder, ServiceDescriptorProto.Builder descriptorProtoBuilder, MethodDescriptorProto.Builder methodProtoBuilder, Set<Descriptor> descritptorSet, Map<String, Integer> methodNames, Method method)
	{
		String methodName = method.getName();
		Integer number = methodNames.get(methodName);
		if (number == null)
		{
			number = 0;
		}
		methodNames.put(methodName, number + 1);

		java.lang.reflect.Type[] argTypes = method.getGenericParameterTypes();
		DescriptorProto.Builder requestProtoBuilder = DescriptorProto.newBuilder();

		String protoName = methodName + "Request";
		if (number > 0)
		{
			protoName = methodName + number + "Request";
		}

		requestProtoBuilder.setName(protoName);

		int i = 0;
		FieldDescriptorProto.Builder methodArgSigsFieldBuilder = FieldDescriptorProto.newBuilder();
		methodArgSigsFieldBuilder.setName("methodArgSigs");
		methodArgSigsFieldBuilder.setNumber(++i);
		StringBuilder methodArgSigs = new StringBuilder();
		for (Class<?> paramType : method.getParameterTypes())
		{
			methodArgSigs.append(paramType.getName()).append("@");
		}
		methodArgSigsFieldBuilder.setLabel(Label.LABEL_OPTIONAL);
		methodArgSigsFieldBuilder.setDefaultValue(methodArgSigs.toString());
		methodArgSigsFieldBuilder.setType(Type.TYPE_STRING);
		requestProtoBuilder.addField(methodArgSigsFieldBuilder.build());

		if (argTypes.length == 0)
		{
			DescriptorProto request = requestProtoBuilder.build();
			fileDescriptorProtoBuilder.addMessageType(request);
			methodProtoBuilder.setInputType(request.getName());
			return;
		}

		for (java.lang.reflect.Type type : argTypes)
		{
			List<FileDescriptor> dependencies = new ArrayList<FileDescriptor>();
			try
			{
				this.fieldConvert(requestProtoBuilder, dependencies, ++i, "arg" + i, type, null);
			} catch (Exception e)
			{
				StringBuilder logBuilder = new StringBuilder();
				logBuilder.append("服务的方法").append(requestProtoBuilder.getName()).append("在生成第").append(i).append("个参数时出错");
				LOGGER.error(logBuilder.toString(),e);
			}
			
			for(FileDescriptor fileDescriptor:dependencies)
			{
				descritptorSet.add(fileDescriptor.getMessageTypes().get(0));
			}
			
		}

		DescriptorProto request = requestProtoBuilder.build();
		fileDescriptorProtoBuilder.addMessageType(request);
		methodProtoBuilder.setInputType(request.getName());
	}

	private void methodReturToOutputTypeDescritptor(FileDescriptorProto.Builder fileDescriptorProtoBuilder, ServiceDescriptorProto.Builder descriptorProtoBuilder, MethodDescriptorProto.Builder methodProtoBuilder, Set<Descriptor> descritptorSet, Map<String, Integer> methodNames, Method method)
	{
		LOGGER.info("pb: 生成接口:" + method.getDeclaringClass() + "方法的:" + method.getName() + "返回值:" + method.getReturnType());

		String methodName = method.getName();
		Integer number = methodNames.get(methodName);
		java.lang.reflect.Type rvalType = method.getGenericReturnType();
		DescriptorProto.Builder responseProtoBuilder = DescriptorProto.newBuilder();
		if (number.equals(1))
		{
			responseProtoBuilder.setName(methodName + "Response");
		} else
		{
			responseProtoBuilder.setName(methodName + (number - 1) + "Response");
		}
		responseProtoBuilder.addField(FieldDescriptorProto.newBuilder().setName("isError").setNumber(2).setType(Type.TYPE_BOOL).setLabel(Label.LABEL_OPTIONAL).setDefaultValue("false"));
		responseProtoBuilder.addField(FieldDescriptorProto.newBuilder().setName("erroMsg").setNumber(3).setType(Type.TYPE_STRING).setLabel(Label.LABEL_OPTIONAL));

		if (rvalType.equals(Void.TYPE))
		{
			DescriptorProto response = responseProtoBuilder.build();

			fileDescriptorProtoBuilder.addMessageType(response);
			methodProtoBuilder.setOutputType(response.getName());
			return;
		}

		List<FileDescriptor> dependencies = new ArrayList<FileDescriptor>();
		try
		{
			this.fieldConvert(responseProtoBuilder, dependencies, 1, "response", rvalType, null);
		} catch (Exception e)
		{
			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append("服务的方法").append(responseProtoBuilder.getName()).append("在生成返回结果时出错");
			LOGGER.error(logBuilder.toString(),e);
		}
		
		for(FileDescriptor fileDescriptor:dependencies)
		{
			descritptorSet.add(fileDescriptor.getMessageTypes().get(0));
		}

		DescriptorProto response = responseProtoBuilder.build();
		fileDescriptorProtoBuilder.addMessageType(response);
		methodProtoBuilder.setOutputType(response.getName());
	}

	public void addFileDescritptor(FileDescriptor fileDescriptor)
	{

		ProtoGenerateContext protoGenerateContext = new ProtoGenerateContext();
		try
		{
			protoGenerateContext.generateProtoFromDescriptor(fileDescriptor);
		} catch (Exception e1)
		{
			//TODO 
			e1.printStackTrace();
		}

		FileDescriptor root = builderContext.getRootFileDescriptor();
		FileDescriptorProto.Builder protoBuilder = root.toProto().toBuilder();
		List<FileDescriptor> oldDependencies = root.getDependencies();

		List<FileDescriptor> dependencies = new ArrayList<FileDescriptor>();
		dependencies.addAll(oldDependencies);
		protoBuilder.addDependency(fileDescriptor.getName());
		dependencies.add(fileDescriptor);
		try
		{
			root = FileDescriptor.buildFrom(protoBuilder.build(), dependencies.toArray(new FileDescriptor[]
			{}));
			builderContext.setRootFileDescriptor(root);
		} catch (DescriptorValidationException e)
		{
			//TODO 
			e.printStackTrace();
		}

	}

	@SuppressWarnings("rawtypes")
	public void fieldToFieldDescriptor(DescriptorProto.Builder descriptorProtoBuilder, List<FileDescriptor> dependencies, int fieldOrder, PropertyDescriptor propertyDescriptor) throws Exception
	{
		java.lang.reflect.Type clazzType = propertyDescriptor.getReadMethod().getGenericReturnType();
		String filedName = propertyDescriptor.getName();
		Class messageClass = propertyDescriptor.getReadMethod().getDeclaringClass();
		fieldConvert(descriptorProtoBuilder, dependencies, fieldOrder, filedName, clazzType, messageClass);
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	private void fieldConvert(DescriptorProto.Builder descriptorProtoBuilder, List<FileDescriptor> dependencies, int fieldOrder, String fieldName, java.lang.reflect.Type clazzType, Class messageClass) throws Exception
	{
		FieldDescriptorProto.Builder fieldDescriptorProtoBuilder = FieldDescriptorProto.newBuilder();
		fieldDescriptorProtoBuilder.setFieldType(clazzType);
		if (clazzType instanceof ParameterizedType)
		{
			ParameterizedType parameterizedType = (ParameterizedType) clazzType;
			Class rawType = (Class) parameterizedType.getRawType();
			if (Collection.class.isAssignableFrom(rawType))
			{
				if (parameterizedType.getActualTypeArguments().length != 1)
				{
					throw new Exception(fieldName + "未曾预料到的集合泛型参数个数:" + parameterizedType.getActualTypeArguments().length);
				}
				Class clazz = null;
				if (parameterizedType.getActualTypeArguments()[0].getClass().getName().equals("sun.reflect.generics.reflectiveObjects.WildcardTypeImpl"))
				{
					clazz = (Class) ((java.lang.reflect.WildcardType) parameterizedType.getActualTypeArguments()[0]).getUpperBounds()[0];
				} else
				{
					clazz = (Class<Serializable>) parameterizedType.getActualTypeArguments()[0];
				}
				Type type = builderContext.getTypeDictionary().getByJavaType(clazz);
				if (type.equals(Type.TYPE_MESSAGE))
				{
					Descriptor descritptor = descriptorFromClass(clazz);
					dependencies.add(descritptor.getFile());
					fieldDescriptorProtoBuilder.setLabel(Label.LABEL_REPEATED).setName(fieldName).setNumber(fieldOrder).setType(Type.TYPE_MESSAGE).setTypeName(clazz.getName());
				} else
				{
					fieldDescriptorProtoBuilder.setLabel(Label.LABEL_REPEATED).setName(fieldName).setNumber(fieldOrder).setType(type);
				}

				descriptorProtoBuilder.addField(fieldDescriptorProtoBuilder.build());
			} else if (Map.class.isAssignableFrom(rawType))
			{
				DescriptorProto.Builder nestDescriptorProtoBuilder = DescriptorProto.newBuilder();
				nestDescriptorProtoBuilder.setName("MapEntry_" + fieldName);
				ParameterizedType pt = (ParameterizedType) clazzType;
				if (pt.getActualTypeArguments().length != 2)
				{
					throw new PBException(fieldName + "未曾预料到的MAP泛型参数个数:" + pt.getActualTypeArguments().length);
				}

				fieldConvert(nestDescriptorProtoBuilder, dependencies, 1, "key", pt.getActualTypeArguments()[0], null);
				fieldConvert(nestDescriptorProtoBuilder, dependencies, 2, "data", pt.getActualTypeArguments()[1], null);
				DescriptorProto mapProto = nestDescriptorProtoBuilder.build();

				descriptorProtoBuilder.addNestedType(mapProto);
				fieldDescriptorProtoBuilder.setLabel(Label.LABEL_REPEATED).setName(fieldName).setNumber(fieldOrder).setType(Type.TYPE_MESSAGE).setTypeName(mapProto.getName());

				descriptorProtoBuilder.addField(fieldDescriptorProtoBuilder.build());
			}
		} else if ((clazzType instanceof Class) && (((Class) clazzType)).isArray())
		{
			Class clazz = ((Class) clazzType).getComponentType();
			if (clazz == null)
			{
				throw new Exception(fieldName + "getter函数array返回值没有确定的数据结构!");
			}
			Type type = builderContext.getTypeDictionary().getByJavaType(clazz);
			if (type.equals(Type.TYPE_MESSAGE))
			{
				// 自包含的情况
				if (!clazz.equals(messageClass))
				{
					Descriptor descritptor = descriptorFromClass(clazz);
					dependencies.add(descritptor.getFile());
				}
				fieldDescriptorProtoBuilder.setLabel(Label.LABEL_REPEATED).setName(fieldName).setNumber(fieldOrder).setType(Type.TYPE_MESSAGE).setTypeName(clazz.getName());
			} else
			{
				fieldDescriptorProtoBuilder.setLabel(Label.LABEL_REPEATED).setName(fieldName).setNumber(fieldOrder).setType(type);
			}

			descriptorProtoBuilder.addField(fieldDescriptorProtoBuilder.build());
		} else if (clazzType instanceof GenericArrayType)
		{
			Class clazz = (Class) (((GenericArrayType) clazzType).getGenericComponentType());
			if (clazz == null)
			{
				throw new Exception(fieldName + "getter函数array返回值没有确定的数据结构!");
			}
			Type type = builderContext.getTypeDictionary().getByJavaType(clazz);
			if (type.equals(Type.TYPE_MESSAGE))
			{
				// 自包含的情况
				if (!clazz.equals(messageClass))
				{
					Descriptor descritptor = descriptorFromClass(clazz);
					dependencies.add(descritptor.getFile());
				}
				fieldDescriptorProtoBuilder.setLabel(Label.LABEL_REPEATED).setName(fieldName).setNumber(fieldOrder).setType(Type.TYPE_MESSAGE).setTypeName(clazz.getName());
			} else
			{
				fieldDescriptorProtoBuilder.setLabel(Label.LABEL_REPEATED).setName(fieldName).setNumber(fieldOrder).setType(type);
			}

			descriptorProtoBuilder.addField(fieldDescriptorProtoBuilder.build());
		} else
		{
			if (clazzType.getClass().getName().equals("sun.reflect.generics.reflectiveObjects.TypeVariableImpl"))
			{
				return;
			}
			Class clazz = (Class) clazzType;
			Type type = builderContext.getTypeDictionary().getByJavaType(clazz);
			if (type == null)
			{
				LOGGER.error("java属性转化为pb描述时，遇到无法解析的java POJO类型:" + clazz + "，该类可能为集合类型!");
				throw new PBException("java属性转化为pb描述时，遇到无法解析的java POJO类型:" + clazz + "，该类可能为集合类型!");
			}
			fieldDescriptorProtoBuilder = FieldDescriptorProto.newBuilder();
			if (type.equals(Type.TYPE_MESSAGE))
			{
				Descriptor descritptor = descriptorFromClass(clazz);
				dependencies.add(descritptor.getFile());
				fieldDescriptorProtoBuilder.setLabel(Label.LABEL_OPTIONAL).setName(fieldName).setNumber(fieldOrder).setType(Type.TYPE_MESSAGE).setTypeName(clazz.getName()).setFieldType(clazzType);
			} else
			{
				fieldDescriptorProtoBuilder.setLabel(Label.LABEL_OPTIONAL).setName(fieldName).setNumber(fieldOrder).setType(type).setFieldType(clazzType);
			}
			
			descriptorProtoBuilder.addField(fieldDescriptorProtoBuilder.build());
		}
	}

}
