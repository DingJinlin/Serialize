package com.taobao.hsf.pb.builder.context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;

public class ProtoGenerateContext
{
	static private final Log LOGGER = LogFactory.getLog("com.taobao.hsf");
	private final static String INFO = "//该文件由系统根据对应的javaben类自动生成protocl buffer文件 如有疑问请联系玄希(xuanxi@taobao.com)";
	private File rootPath;

	public ProtoGenerateContext()
	{
		rootPath = new File(System.getProperty("user.home") + "/protocolbuffers_proto");
		if (!rootPath.exists())
			rootPath.mkdirs();
		LOGGER.error("HSF服务生存的protocol buffer文件存放于:" + rootPath.getAbsolutePath() + "目录下!");
	}

	void generateProtoFromDescriptor(final FileDescriptor fileDescriptor) throws Exception
	{

		String packageName = fileDescriptor.getPackage();
		File pacagePath = rootPath;
		if (packageName != null || !"".equals(packageName))
		{
			pacagePath = new File(rootPath, packageName.replace(".", "/"));
			if (!pacagePath.exists())
			{
				pacagePath.mkdirs();
			}
		}

		String fileName = fileDescriptor.getName();

		File descritptorFile = new File(pacagePath, fileName.replace(packageName + ".", "") + ".proto");
		if (descritptorFile.exists())
		{
			descritptorFile.delete();
		}
		descritptorFile.createNewFile();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(INFO + "\r\n");
		stringBuilder.append("package " + packageName + ";\r\n");

		for (FileDescriptor importFileDescrptor : fileDescriptor.getDependencies())
		{
			stringBuilder.append("import \"" + importFileDescrptor.getName().replace(".", "/") + ".proto\";\r\n");
		}

		stringBuilder.append("option java_outer_classname = \"" + fileName.replace(packageName + ".", "") + "SB\";\r\n");

		for (Descriptor descritpor : fileDescriptor.getMessageTypes())
		{

			stringBuilder.append(buildDescritpor(descritpor));
		}

		for (ServiceDescriptor serviceDescriptor : fileDescriptor.getServices())
		{
			stringBuilder.append(buildServiceDescriptor(serviceDescriptor));
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(descritptorFile));
		writer.write(stringBuilder.toString());
		writer.flush();
		writer.close();
	}

	private String buildServiceDescriptor(ServiceDescriptor serviceDescriptor)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("service " + serviceDescriptor.getName() + "{\r\n");
		for (MethodDescriptor methodDescriptor : serviceDescriptor.getMethods())
		{
			builder.append("rpc " + methodDescriptor.getName() + "(" + methodDescriptor.getInputType().getName() + ") returns ("
					+ methodDescriptor.getOutputType().getName() + ");\r\n");
		}

		builder.append("}\r\n");
		return builder.toString();
	}

	private String buildDescritpor(Descriptor descriptor)
	{

		StringBuilder builder = new StringBuilder();

		builder.append("message " + descriptor.getName() + "\r\n");
		builder.append("{\r\n");

		for (Descriptor nestDescriptor : descriptor.getNestedTypes())
		{
			builder.append(buildDescritpor(nestDescriptor));
		}

		for (FieldDescriptor field : descriptor.getFields())
		{

			if (field.isRepeated())
			{
				builder.append("repeated ");
			}
			else if(field.isRequired())
			{
				builder.append("required ");
			}
			else
			{
				builder.append("optional ");
			}

			if (field.toProto().getTypeName() == null || field.toProto().getTypeName().equals(""))
			{
				com.google.protobuf.Descriptors.FieldDescriptor.Type type = field.getType();
				builder.append(" " + type.toString().toLowerCase() + " ");
			}
			else
			{
				builder.append(field.toProto().getTypeName() + " ");

			}
			
			builder.append(field.getName() + " = " + field.getNumber());
			if(field.hasDefaultValue())
			{
				if(field.getType().equals(com.google.protobuf.Descriptors.FieldDescriptor.Type.STRING))
				{
					builder.append("[default = \"" + field.getDefaultValue() +"\"]");
				}
				else
				{
					builder.append("[default = " + field.getDefaultValue() +"]");
				}
			}
			builder.append(";\r\n");
			
		}

		builder.append("}\r\n");
		return builder.toString();
	}
}
