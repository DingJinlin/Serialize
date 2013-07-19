package com.taobao.hsf.pb.builder.spi;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

public interface MessageBuilder
{
	Message buildMessage(Descriptor descriptor, Object object);

	void buildMessageField(Message.Builder dynamicBuilder, FieldDescriptor fieldDescritptor, Object value);

}
