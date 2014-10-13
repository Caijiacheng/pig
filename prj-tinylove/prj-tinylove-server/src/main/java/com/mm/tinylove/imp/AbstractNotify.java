package com.mm.tinylove.imp;

import com.google.protobuf.ByteString;
import com.mm.tinylove.INotify;
import com.mm.tinylove.proto.Storage.Notify;

public abstract class AbstractNotify extends ProtoStorage<Notify> implements
		INotify<Notify.Type> {

	public AbstractNotify(long id) {
		super(id);
	}

	abstract protected byte[] marshalNotifyValue();

	abstract protected void unmarshalNotifyValue(byte[] value);

	public Notify.Type type() {
		return getProto().getType();
	}

	@Override
	public byte[] marshalValue() {
		value = getProto().toBuilder()
				.setValue(ByteString.copyFrom(marshalNotifyValue())).build();
		return super.marshalValue();
	}

	@Override
	public void unmarshalValue(byte[] data) {
		super.unmarshalValue(data);
		unmarshalNotifyValue(getProto().getValue().toByteArray());
	}

}
