package com.mm.tinylove.imp;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Comment;

public class DefaultComment extends ProtoStorage<Comment.Builder> implements IComment{

	public DefaultComment(long id) {
		super(id, Comment.newBuilder());
	}
	
	static DefaultComment create()
	{
		return new DefaultComment(KVStorage.INVAID_KEY);
	}
	
	static final public String TAG_COMMENT_PRISE = ":priser";

	IRangeList<Long> getCommentPriserIds() {
		return new LongRangeList(getKey() + TAG_COMMENT_PRISE);
	}
	
	@Override
	public IMessage parent() {
		return Ins.getIMessage(getProto().getMsgid());
	}

	@Override
	public IUser user() {
		return Ins.getIUser(getProto().getUserid());
	}

	@Override
	public IRangeList<IUser> prisers() {
		return new ImmutableObjectRangeList<IUser>(getCommentPriserIds()) {
			public IUser apply(Long id) {
				return Ins.getIUser(id);
			}
		};
	}

}
