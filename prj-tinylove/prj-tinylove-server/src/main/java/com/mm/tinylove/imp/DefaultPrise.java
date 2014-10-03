package com.mm.tinylove.imp;

import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPrise;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Prise;

@Deprecated
public class DefaultPrise extends ProtoStorage<Prise.Builder> implements IPrise{

	public DefaultPrise(long id) {
		super(id, Prise.newBuilder());
	}

	
	static DefaultPrise create()
	{
		return new DefaultPrise(INVAID_KEY);
	}
	
	static DefaultPrise create(IUser user, IMessage msg)
	{
		DefaultPrise prise = new DefaultPrise(INVAID_KEY);
		prise.getProto().setUserid(user.id());
		prise.getProto().setMsgid(msg.id());
		return prise;
	}
	
	@Override
	public IUser user() {
		return Ins.getIUser(getProto().getUserid());
	}


	@Override
	public IMessage msg() {
		return Ins.getIMessage(getProto().getMsgid());
	}


}
