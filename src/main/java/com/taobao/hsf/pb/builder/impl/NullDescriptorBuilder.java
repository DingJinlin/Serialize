package com.taobao.hsf.pb.builder.impl;

import com.google.protobuf.Descriptors.Descriptor;
import com.taobao.hsf.pb.builder.context.DescriptorGenerateContext;
import com.taobao.hsf.pb.builder.spi.Builder;

/**
 * û������������������ �� ���� map ԭ������
 * @author xuanxi
 *
 */
public abstract class NullDescriptorBuilder implements Builder
{
	public Descriptor buildDescriptor(DescriptorGenerateContext context, Class Clazz)
	{
		return null;
	}
}
