package com.mm.tinylove.imp;

import com.google.common.base.Verify;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IStory;
import com.mm.tinylove.proto.Storage.Story;

public class DefaultStory extends FollowStorage<Story.Builder> implements IStory {

	public DefaultStory(long id) {
		super(id, Story.newBuilder());
	}

	static DefaultStory create(long userid, long pairid)
	{
		Verify.verify(pairid != INVAID_KEY);
		Verify.verify(userid != INVAID_KEY);
		DefaultStory s = new DefaultStory(INVAID_KEY);
		s.getProto().setUserid(userid).setPairid(pairid);
		return s;
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
