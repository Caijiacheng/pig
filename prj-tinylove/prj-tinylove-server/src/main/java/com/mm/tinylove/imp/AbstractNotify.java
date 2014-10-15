package com.mm.tinylove.imp;

import com.google.protobuf.ByteString;
import com.mm.tinylove.INotify;
import com.mm.tinylove.proto.Storage.Notify;

public abstract class AbstractNotify extends ProtoStorage<Notify> implements
		INotify<Notify.Type> {

	public AbstractNotify(long id) {
		super(id);
	}

	
	@Override
	String uniqKey() {
		return "AbstractNotify";
	}
	
	abstract protected byte[] marshalNotifyValue();

	abstract protected void unmarshalNotifyValue(byte[] value);

	public Notify.Type type() {
		return getProto().getType();
	}
	
	public void rebuildNotify(Notify.Type t)
	{
		Notify.Builder builder = getKBuilder();
		value = builder.setType(t).setValue(ByteString.copyFrom(marshalNotifyValue())).build();
	}
	
	@Override
	public void unmarshalValue(byte[] data) {
		super.unmarshalValue(data);
		unmarshalNotifyValue(getProto().getValue().toByteArray());
	}

}
