package com.mm.tinylove.imp;

import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IStory;
import com.mm.tinylove.proto.Storage.Story;

public class DefaultStory extends ProtoStorage<Story.Builder> implements IStory {

	public DefaultStory(long id) {
		super(id, Story.newBuilder());
	}

	static DefaultStory create()
	{
		return new DefaultStory(INVAID_KEY);
	}
	
	
	static String MSG_TAG = ":messages";
	
	
	IRangeList<Long> getStorysMessagesIDs()
	{
		return new LongRangeList(getKey() + MSG_TAG);
	}
	
	
	@Override
	public IRangeList<IMessage> message() {

		return new ImmutableObjectRangeList<IMessage>(getStorysMessagesIDs()) 
				{
				public IMessage apply(Long id)
				{
					return Ins.getIMessage(id);
				}
		};
	}

	@Override
	public IPair pair() {
		return Ins.getIPair(value.getPairid());
	}

}
