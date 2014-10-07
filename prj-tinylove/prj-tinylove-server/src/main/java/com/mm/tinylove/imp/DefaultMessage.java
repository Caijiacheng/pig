package com.mm.tinylove.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mm.tinylove.IComment;
import com.mm.tinylove.ILocation;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IStory;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Msg;

public class DefaultMessage extends ProtoStorage<Msg.Builder> implements
		IMessage {

	static Logger LOG = LoggerFactory.getLogger(DefaultMessage.class);

	public DefaultMessage(long id) {
		super(id, Msg.newBuilder());
	}

	static DefaultMessage create() {
		return new DefaultMessage(KVStorage.INVAID_KEY);
	}

	@Override
	public IUser publisher() {
		return Ins.getIUser(getProto().getUserid());
	}

	@Override
	public IPair pair() {
		return Ins.getIPair(getProto().getPairid());
	}

	static final public String TAG_MSG_COMMENTS = ":comments";
	static final public String TAG_MSG_PRISES = ":prisers";

	IRangeList<Long> getMsgCommentsIds() {
		return new LongRangeList(getKey() + TAG_MSG_COMMENTS);
	}

	IRangeList<Long> getMsgPriserIds() {
		return new LongRangeList(getKey() + TAG_MSG_PRISES);
	}

	@Override
	public IRangeList<IComment> comments() {
		return new ImmutableObjectRangeList<IComment>(getMsgCommentsIds()) {
			public IComment apply(Long id) {
				return Ins.getIComment(id);
			}
		};
	}

	// @Override
	// public IRangeList<IPrise> prises() {
	//
	// return new ImmutableObjectRangeList<IPrise>(getMsgPriseIds()) {
	// public IPrise apply(Long id) {
	// return Ins.getIPrise(id);
	// }
	// };
	// }

	@Override
	public IStory parent() {
		return Ins.getIStory(getProto().getStoryid());
	}

	@Override
	public ILocation location() {
		return new DefaultLocation(getProto().getLocation());
	}

	@Override
	public IRangeList<IUser> prisers() {
		return new ImmutableObjectRangeList<IUser>(getMsgPriserIds()) {
			public IUser apply(Long id) {
				return Ins.getIUser(id);
			}
		};
	}

	@Override
	public String content() {
		return getProto().getContent();
	}

	@Override
	public String imgurl() {
		return getProto().getPhotouri();
	}

	@Override
	public String videourl() {
		return getProto().getVideouri();
	}

	@Override
	public long timestamp() {
		return getProto().getTimestamp();
	}

}
