package com.mm.tinylove.notify;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IUser;
import com.mm.tinylove.event.MessageEvent;

public class NotifyEventTrigger {

	public NotifyEventTrigger() {

	}

	public void MessageNewComment(MessageEvent.AddComment change) {

		IMessage msg = change.getMsg();
		IComment comment = change.getComment();
		
		NewCommentNotify notify = NewCommentNotify.create(msg.id(),
				comment.id());
		
		// message user add notify
		IUser creator = change.getMsg().publisher();
		
		
		creator.userNotifys().lpush(notify);
		// message follower add notify
		for (IUser follower : msg.followers().all())
		{
			follower.userNotifys().lpush(notify);
		}
		// comment user follow the message
		IUser commentor = comment.user();
		commentor.follow(msg);
		
		
		
	}

}
