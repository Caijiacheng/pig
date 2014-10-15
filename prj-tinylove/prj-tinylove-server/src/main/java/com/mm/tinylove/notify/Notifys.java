package com.mm.tinylove.notify;

import com.google.common.base.Preconditions;
import com.mm.tinylove.INotify;
import com.mm.tinylove.imp.AbstractNotify;
import com.mm.tinylove.proto.Storage.Notify;

final public class Notifys extends AbstractNotify{

	public Notifys(long id) {
		super(id);
	}
	
	INotify<Notify.Type> typeNotify;
	
	public INotify<Notify.Type> ins()
	{
		return Preconditions.checkNotNull(typeNotify);
	}
	
	@Override
	protected byte[] marshalNotifyValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void unmarshalNotifyValue(byte[] value) {
		typeNotify = transform(value);
	}
	
	private INotify<Notify.Type> transform(byte[] value)
	{
		switch (getProto().getType()) {
		case NEW_COMMENT:
			NewCommentNotify notify_comment = new NewCommentNotify(id());
			notify_comment.setValue(getProto());
			notify_comment.unmarshalValue(value);
			return notify_comment;
		case NEW_PRISE:
			NewPriseNotify notify_prise = new NewPriseNotify(id());
			notify_prise.setValue(getProto());
			notify_prise.unmarshalValue(value);
			return notify_prise;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	
}
