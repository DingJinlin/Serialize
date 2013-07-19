package com.taobao.hsf.pb.builder.spi;

import com.google.protobuf.Descriptors.Descriptor;
import com.taobao.hsf.pb.builder.context.DescriptorGenerateContext;

public interface DescriptorBuilder
{

	Descriptor buildDescriptor(DescriptorGenerateContext context, Class Clazz);
}
