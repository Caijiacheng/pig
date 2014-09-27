package com.mm.tinylove.imp;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.mm.tinylove.error.NotExistException;
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
	
	@Override
	public byte[] marshalValue() {
		return Preconditions.checkNotNull(value).build().toByteArray();
	}
	
	@Override
	public void unmarshalValue(byte[] data) {
		try {
			Preconditions.checkNotNull(value).mergeFrom(data);
		} catch (InvalidProtocolBufferException e) {
			throw new UnmarshalException(e);
		}
	}


}
