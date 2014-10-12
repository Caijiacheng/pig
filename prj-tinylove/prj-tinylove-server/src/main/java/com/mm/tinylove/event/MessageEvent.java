package com.mm.tinylove.event;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IUser;

public class MessageEvent {
	IMessage msg;
	MessageEvent(IMessage msg) {
		this.msg = msg;
	}
	
	
	public IMessage getMsg()
	{
		return this.msg;
	}
			
	
	public static class Creater extends MessageEvent
	{
		public Creater(IMessage msg) {
			super(msg);
		}
	}
	
	public static class AddComment extends MessageEvent
	{
		public AddComment(IMessage msg, IComment comment) {
			super(msg);
			this.comment = comment;
		}
		IComment comment;
		
		public IComment getComment()
		{
			return comment;
		}
	}
	
	public static class AddPrise extends MessageEvent
	{
		IUser priser;
		public AddPrise(IMessage msg, IUser priser)
		{
			super(msg);
			this.priser = priser;
		}
		
		public IUser getPriser()
		{
			return priser;
		}
	}
}
