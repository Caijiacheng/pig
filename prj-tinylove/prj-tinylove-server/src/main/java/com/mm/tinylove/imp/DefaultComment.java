package com.mm.tinylove.imp;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Comment;

public class DefaultComment extends ProtoStorage<Comment.Builder> implements IComment{

	public DefaultComment(long id) {
		super(id, Comment.newBuilder());
	}

	@Override
	public IMessage parent() {
		return Ins.getIMessage(getProto().getMsgid());
	}

	@Override
	public IUser user() {
		return Ins.getIUser(getProto().getUserid());
	}

}
