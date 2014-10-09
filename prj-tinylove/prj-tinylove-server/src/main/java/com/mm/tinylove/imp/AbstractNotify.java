package com.mm.tinylove.imp;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.mm.tinylove.INotify;
import com.mm.tinylove.proto.Storage.Notify;

public abstract class AbstractNotify extends ProtoStorage<Notify.Builder> implements INotify<Notify.Type>{

	public AbstractNotify(long id, Notify.Builder builder) {
		super(id, builder);
	}

	abstract protected byte[] marshalNotifyValue();
	abstract protected void unmarshalNotifyValue(byte[] value);
	
	public Notify.Type type()
	{
		return getProto().getType();
	}
	
	
	static <T, K> byte[] marshalMaps(Map<T, K> ids)
	{
		return new Gson().toJson(ids).getBytes(StandardCharsets.UTF_8);
	}
	
	
	@Override
	public byte[] marshalValue() {
		getProto().setValue(ByteString.copyFrom(marshalNotifyValue()));
		return super.marshalValue();
	}
	
	@Override
	public void unmarshalValue(byte[] data) {
		super.unmarshalValue(data);
		unmarshalNotifyValue(getProto().getValue().toByteArray());
	}
	
	
	
}
