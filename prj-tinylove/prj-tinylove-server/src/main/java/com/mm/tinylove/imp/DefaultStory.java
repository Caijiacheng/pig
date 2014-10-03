package com.mm.tinylove.imp;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
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
	
	
	List<Long> getMsgListIDs()
	{
		return new ListStorage0(getKey()+":msgids");
	}
	@Override
	public List<IMessage> message() {

		return Lists.transform(getMsgListIDs(),
				new Function<Long, IMessage>() {
					public IMessage apply(Long id) {
						return Ins.getIMessage(id);
					}
				});
	}

	@Override
	public IPair pair() {
		return Ins.getIPair(value.getPairid());
	}

}
