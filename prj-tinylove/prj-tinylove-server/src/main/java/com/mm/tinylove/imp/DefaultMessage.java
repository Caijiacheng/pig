package com.mm.tinylove.imp;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mm.tinylove.IComment;
import com.mm.tinylove.ILocation;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IStory;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Msg;

public class DefaultMessage extends ProtoStorage<Msg.Builder> implements
		IMessage {

	static Logger LOG = LoggerFactory.getLogger(DefaultMessage.class);
	
	public DefaultMessage(long id) {
		super(id, Msg.newBuilder());
	}

	static DefaultMessage create()
	{
		return new DefaultMessage(KVStorage.INVAID_KEY);
	}

	List<Long> getCommentListIDs()
	{
		return new ListStorage0(getKey() + ":comment");
	}

	@Override
	public IUser publisher() {
		return Ins.getIUser(getProto().getUserid());
	}
	
	@Override
	public IPair pair() {
		return Ins.getIPair(getProto().getPairid());
	}

	@Override
	public List<IComment> comment() {
		return Lists.transform(getCommentListIDs(),
				new Function<Long, IComment>() {
					public IComment apply(Long input) {
						return Ins.getIComment(input);
					}
				});
	}

	@Override
	public IStory parent() {
		return Ins.getIStory(getProto().getStoryid());
	}

	@Override
	public ILocation location() {
		return new DefaultLocation(getProto().getLocation());
	}

}
