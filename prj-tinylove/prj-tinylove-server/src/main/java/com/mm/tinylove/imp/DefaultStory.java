package com.mm.tinylove.imp;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IStory;
import com.mm.tinylove.proto.Storage.Story;

public class DefaultStory extends ProtoStorage<Story.Builder> implements IStory {

	public DefaultStory(long id) {
		super(id, Story.newBuilder());
	}

	@Override
	public List<IMessage> message() {

		return Lists.transform(getProto().getMsgidsList(),
				new Function<Long, IMessage>() {
					public IMessage apply(Long id) {
						return Ins.getIMessage(id);
					}
				});
	}

}
