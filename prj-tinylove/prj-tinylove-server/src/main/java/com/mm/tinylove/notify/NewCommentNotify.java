package com.mm.tinylove.notify;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.imp.Ins;
import com.mm.tinylove.proto.Storage.Notify;

public class NewCommentNotify extends AbsBundleNotify {

	public NewCommentNotify(long id) {
		super(id, Notify.newBuilder().setType(Notify.Type.NEW_COMMENT));
	}

	static public NewCommentNotify create(long msgid, long commentid) {
		NewCommentNotify notify = new NewCommentNotify(INVAID_KEY);
		notify.bundle.put(K_MESSAGE, msgid);
		notify.bundle.put(K_COMMENT, commentid);
		return notify;
	}

	public IMessage getIMessage() {
		return Ins.getIMessage(bundle.get(K_MESSAGE));
	}

	public IComment getIComment() {
		return Ins.getIComment(bundle.get(K_COMMENT));
	}

	public static final String K_MESSAGE = "IMESSAGE";
	public static final String K_COMMENT = "ICOMMENT";

	@Override
	String[] verifyBundleKeys() {
		return new String[] { K_MESSAGE, K_COMMENT };
	}

}
