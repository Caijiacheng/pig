package com.mm.tinylove.imp;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Comment;

public class DefaultComment extends FollowStorage<Comment> implements IComment{

	public DefaultComment(long id) {
		super(id);
	}
	
	static DefaultComment create(IMessage msg, IUser user, String comment)
	{
		DefaultComment c =  new DefaultComment(KVStorage.INVAID_KEY);
		Comment.Builder builder = c.getKBuilder();
		builder.setMsgid(msg.id());
		builder.setUserid(user.id());
		builder.setContent(comment);
		c.rebuildValueAndBrokenImmutable(builder);
		return c;
	}
	
	static final public String TAG_COMMENT_PRISE = ":priser";

	IRangeList<Long> getCommentPriserIds() {
		return LongRangeList.getIns(getKey() + TAG_COMMENT_PRISE);
	}
	
	@Override
	public IMessage msg() {
		return Ins.getIMessage(getProto().getMsgid());
	}

	@Override
	public IUser user() {
		return Ins.getIUser(getProto().getUserid());
	}

	@Override
	public IRangeList<IUser> prisers() {
		return new ObjectRangeList<IUser>(getCommentPriserIds()) {
			public IUser apply(Long id) {
				return Ins.getIUser(id);
			}
		};
	}

	@Override
	public String content() {
		return getProto().getContent();
	}

}
