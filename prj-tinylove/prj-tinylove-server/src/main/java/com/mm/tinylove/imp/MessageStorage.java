package com.mm.tinylove.imp;

import com.mm.tinylove.IMessage;
import com.mm.tinylove.IRangeList;

public class MessageStorage extends LongRangeList{
	
	static String MSG_STORAGE_TAG = "MsgRangeList";
	
	public MessageStorage() {
		super(MSG_STORAGE_TAG);
	}
	
	
	public IRangeList<IMessage> messageList()
	{
		return new ObjectRangeList<IMessage>(this, null) {
			public IMessage apply(Long id) {
				return Ins.getIMessage(id);
			}

		};
	}
	
	
}
