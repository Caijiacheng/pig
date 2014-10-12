package com.mm.tinylove.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.mm.tinylove.IObject;
import com.mm.tinylove.error.UnmarshalException;

public class ProtoStorage<T extends Message.Builder> extends CollectionStorage {

	static Logger LOG = LoggerFactory.getLogger(ProtoStorage.class);

	protected T value;

	public ProtoStorage(long id, T ins) {
		super(id);
		value = ins;
	}

	T getProto() {
		return value;
	}

	// 这里先toByteString(),再ToStringUtf8().主要是为了让数据进行压缩序列化
	@Override
	public byte[] marshalValue() {
		return Preconditions.checkNotNull(value).build().toByteArray();
	}

	@Override
	public void unmarshalValue(byte[] data) {
		try {
			Preconditions.checkNotNull(value).mergeFrom(
					data);

		} catch (InvalidProtocolBufferException e) {
			throw new UnmarshalException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IObject) {
			IObject o = (IObject) obj;
			return o.id() == id();
		}
		return super.equals(obj);
	}

	

}
