package com.mm.tinylove.notify;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.imp.Ins;
import com.mm.tinylove.proto.Storage.Notify;

public class NewCommentNotify extends AbsBundleNotify {

	NewCommentNotify(long id) {
		super(id);
	}

	static public NewCommentNotify create(long msgid, long commentid) {
		NewCommentNotify notify = new NewCommentNotify(INVAID_KEY);
		notify.bundle.put(K_MESSAGE, Long.valueOf(msgid));
		notify.bundle.put(K_COMMENT, Long.valueOf(commentid));
		notify.rebuildNotify(Notify.Type.NEW_COMMENT);
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
