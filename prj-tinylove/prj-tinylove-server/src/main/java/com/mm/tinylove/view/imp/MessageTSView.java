package com.mm.tinylove.view.imp;

import com.mm.tinylove.IMessage;
import com.mm.tinylove.view.MessageView;

/**
 * 按发布时间排序的Message
 * @author apple
 *
 */


public class MessageTSView extends MessageView{

	
	static final public String MSG_TS_SORT_VIEW = "MESSAGE:TS:VIEW";
	
	public MessageTSView() {
		super(MSG_TS_SORT_VIEW);
	}

	@Override
	protected double getScore(IMessage obj) {
		return -obj.timestamp();
	}
	
}
