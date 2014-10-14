package com.mm.tinylove.notify;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IUser;
import com.mm.tinylove.event.MessageEvent;
import com.mm.tinylove.imp.StorageSaveRunnable;

public class NotifyEventTrigger {

	public NotifyEventTrigger() {

	}

	public void messageNewCommentTrigger(final MessageEvent.AddComment change) {
		new StorageSaveRunnable() {
			@Override
			protected Object onSaveTransactionRun() {

				IMessage msg = change.getMsg();
				IComment comment = change.getComment();
				NewCommentNotify notify = NewCommentNotify.create(msg.id(),
						comment.id());
				IUser creator = msg.publisher();
				creator.userNotifys().lpush(notify);
				for (IUser follower : msg.followers().all()) {
					follower.userNotifys().lpush(notify);
				}
				IUser commentor = comment.user();
				commentor.follow(msg);
				return null;
			}
		}.run();
	}

	public void messageNewPriseTrigger(final MessageEvent.AddPrise ev_prise) {
		new StorageSaveRunnable() {
			@Override
			protected Object onSaveTransactionRun() {

				IMessage msg = ev_prise.getMsg();
				IUser priser = ev_prise.getPriser();
				NewPriseNotify notify = NewPriseNotify.create(priser
						.id(), msg.id());
				IUser creator = msg.publisher();
				creator.userNotifys().lpush(notify);
				for (IUser follower : msg.followers().all()) {
					follower.userNotifys().lpush(notify);
				}
				priser.follow(msg);
				return null;
			}
		}.run();
	}

}
