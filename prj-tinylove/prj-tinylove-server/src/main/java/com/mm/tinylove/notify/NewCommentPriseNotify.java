package com.mm.tinylove.notify;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IUser;
import com.mm.tinylove.imp.Ins;
import com.mm.tinylove.proto.Storage.Notify;
import com.mm.tinylove.proto.Storage.Notify.Builder;

public class NewCommentPriseNotify extends AbsBundleNotify {

	public NewCommentPriseNotify(long id, Builder builder) {
		super(id, Notify.newBuilder().setType(Notify.Type.NEW_COMMENT_PRISE));
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
