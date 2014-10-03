package com.mm.tinylove.imp;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.mm.tinylove.error.UnmarshalException;

public class ProtoStorage<T extends Message.Builder> extends KVStorage{

	protected T value;
	
	public ProtoStorage(long id, T ins) {
		super(id);
		value = ins;
	}
	
	
	T getProto()
	{
		return value;
	}
	
	//这里先toByteString(),再ToStringUtf8().主要是为了让数据进行压缩序列化
	@Override
	public String marshalValue() {
		return Preconditions.checkNotNull(value).build().toByteString().toStringUtf8();
	}
	
	@Override
	public void unmarshalValue(String data) {
		try {
			Preconditions.checkNotNull(value).mergeFrom(ByteString.copyFromUtf8(data));
		} catch (InvalidProtocolBufferException e) {
			throw new UnmarshalException(e);
		}
	}


}
