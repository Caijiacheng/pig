package com.mm.tinylove.event;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IUser;

public class CommentEvent {
	IComment comment;
	
	CommentEvent(IComment comment)
	{
		this.comment = comment;
	}
	
	public static class AddPrise extends CommentEvent
	{
		public AddPrise(IComment comment, IUser priser)
		{
			super(comment);
			this.priser = priser;
		}
		IUser priser;
	}
	
	
}
