package com.mm.tinylove.notify;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IUser;
import com.mm.tinylove.imp.Ins;
import com.mm.tinylove.proto.Storage.Notify;

public class NewCommentPriseNotify extends AbsBundleNotify {

	public NewCommentPriseNotify(long id) {
		super(id);
	}
	
	static public NewCommentPriseNotify create(long priserid, long commentid) {
		NewCommentPriseNotify notify = new NewCommentPriseNotify(INVAID_KEY);
		notify.bundle.put(K_PRISER, priserid);
		notify.bundle.put(K_COMMENT, commentid);
		Notify.Builder builder = notify.getKBuilder();
		notify.rebuildValueAndBrokenImmutable(builder
				.setType(Notify.Type.NEW_COMMENT_PRISE));
		return notify;
	}
	
	public IComment getIComment() {
		return Ins.getIComment(bundle.get(K_COMMENT));
	}

	public IUser getIUser() {
		return Ins.getIUser(bundle.get(K_PRISER));
	}

	public static final String K_COMMENT = "ICOMMENT";
	public static final String K_PRISER = "IPRISER";

	@Override
	String[] verifyBundleKeys() {
		return new String[] { K_COMMENT, K_COMMENT };
	}

}
