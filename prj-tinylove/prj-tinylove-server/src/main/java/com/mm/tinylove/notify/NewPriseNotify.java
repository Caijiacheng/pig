package com.mm.tinylove.notify;

import com.mm.tinylove.IMessage;
import com.mm.tinylove.IUser;
import com.mm.tinylove.imp.Ins;
import com.mm.tinylove.proto.Storage.Notify;

public class NewPriseNotify extends AbsBundleNotify{

	NewPriseNotify(long id) {
		super(id);
	}
	
	static public NewPriseNotify create(long priserid, long msgid) {
		NewPriseNotify notify = new NewPriseNotify(INVAID_KEY);
		notify.bundle.put(K_PRISER, priserid);
		notify.bundle.put(K_MESSAGE, msgid);
		notify.rebuildNotify(Notify.Type.NEW_PRISE);
		return notify;
	}
	
	public IMessage getIMessage()
	{
		return Ins.getIMessage(bundle.get(K_MESSAGE));
	}
	
	public IUser getIUser()
	{
		return Ins.getIUser(bundle.get(K_PRISER));
	}
	
	public static final String K_MESSAGE = "IMESSAGE";
	public static final String K_PRISER = "IPRISER";
	
	@Override
	String[] verifyBundleKeys() {
		return new String[]{K_MESSAGE, K_PRISER};
	}

}
