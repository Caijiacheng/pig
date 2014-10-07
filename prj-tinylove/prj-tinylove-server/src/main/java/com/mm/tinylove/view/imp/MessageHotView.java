package com.mm.tinylove.view.imp;

import com.mm.tinylove.IMessage;
import com.mm.tinylove.view.MessageView;

/**
 * 按帖子热度排序
 * 
 * @author apple
 * 
 */
public class MessageHotView extends MessageView {

	static final public String MSG_HOT_SORT_VIEW = "MESSAGE:HOT:VIEW";

	public MessageHotView() {
		super(MSG_HOT_SORT_VIEW);
	}

	@Override
	protected double getScore(IMessage obj) {
		return -(obj.comments().size() + obj.prisers().size());
	}
	

}
