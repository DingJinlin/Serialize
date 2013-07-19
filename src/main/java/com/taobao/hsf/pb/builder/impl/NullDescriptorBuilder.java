package com.taobao.hsf.pb.builder.impl;

import com.google.protobuf.Descriptors.Descriptor;
import com.taobao.hsf.pb.builder.context.DescriptorGenerateContext;
import com.taobao.hsf.pb.builder.spi.Builder;

/**
 * 没有生成描述的生成器 如 数据 map 原生类型
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
