package com.mm.tinylove.notify;

import java.util.List;

import com.google.common.collect.Lists;
import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IUser;
import com.mm.tinylove.event.MessageEvent;
import com.mm.tinylove.imp.Ins;

public class NotifyEventTrigger {

	public NotifyEventTrigger() {

	}

	public void MessageNewComment(MessageEvent.AddComment change) {
		
		IMessage msg = change.getMsg();
		IComment comment = change.getComment();
		
		List<Object> ins_to_save = Lists.newArrayList();

		NewCommentNotify notify = NewCommentNotify.create(msg.id(),
				comment.id());
		ins_to_save.add(notify);
		// message user add notify
		IUser creator = change.getMsg().publisher();
		creator.userNotifys().lpush(notify);
		ins_to_save.add(creator);
		// message follower add notify
		for (IUser follower : msg.followers().all())
		{
			follower.userNotifys().lpush(notify);
			ins_to_save.add(follower);
		}
		// comment user follow the message
		IUser commentor = comment.user();
		commentor.follow(msg);
		ins_to_save.add(commentor);
		
		Ins.getStorageService().checkAndSaveInTransaction(ins_to_save);
	}

}
