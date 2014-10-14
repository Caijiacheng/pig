package com.mm.tinylove.imp;

import com.mm.tinylove.IMessage;
import com.mm.tinylove.IRangeList;

public class MessageStorage extends LongRangeList{
	
	static String MSG_STORAGE_TAG = "MsgRangeList";
	
	
	static ThreadLocal<MessageStorage> TL_MESSAGE_STORAGE = new ThreadLocal<MessageStorage>()
			{
				protected MessageStorage initialValue() {
					return new MessageStorage();
				};
			};
	
	static public MessageStorage getIns()
	{
		return TL_MESSAGE_STORAGE.get();
	}
	
	public MessageStorage() {
		super(MSG_STORAGE_TAG);
	}
	
	
	public IRangeList<IMessage> messageList()
	{
		return new ObjectRangeList<IMessage>(this) {
			public IMessage apply(Long id) {
				return Ins.getIMessage(id);
			}

		};
	}
	
	
}
