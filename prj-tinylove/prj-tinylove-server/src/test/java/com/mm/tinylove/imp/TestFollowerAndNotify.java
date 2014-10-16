package com.mm.tinylove.imp;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mm.tinylove.ILocation;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.INotify;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IUser;
import com.mm.tinylove.notify.NewCommentNotify;
import com.mm.tinylove.notify.NewPriseNotify;
import com.mm.tinylove.proto.Storage.Location;
import com.mm.tinylove.proto.Storage.Notify;
import com.mm.tinylove.proto.Storage.Notify.Type;

public class TestFollowerAndNotify {

	
	static Logger LOG = LoggerFactory.getLogger(TestFollowerAndNotify.class); 
	
	@Before
	public void setup() {
		Ins.s_storage_service = new RemoveStorageService();
		Ins.getStorageService().cleanStorage();
	}

	@Test
	public void testCommentMessageAndNotify() throws InterruptedException {
		String imgurl = "http://www.hello.img.com";
		ILocation location = new DefaultLocation(Location.newBuilder()
				.setX(1.0f).setY(2.0f).build());

		IUser owner = UserStorage.createUserAndSave();
		IPair pair = owner.createPair("tt");
		final IMessage msg = owner.publishMsg(pair, "i am here", location,
				imgurl, null);

		int user_num = 100;

		Set<IUser> users = Sets.newHashSet();

		for (int i = 0; i < user_num; i++) {
			IUser u = UserStorage.createUserAndSave();
			u.publishComment(msg, "pppp");
			u.publishPrise(msg);
			users.add(u);
		}
		Thread.sleep(1000);// wait for notify
		Assert.assertTrue(Iterators.all(users.iterator(),
				new Predicate<IUser>() {
					public boolean apply(IUser ins) {
						return msg.followers().exist(ins);
					}
				}));

		Assert.assertEquals(owner.userNotifys().size(), user_num * 2);

		int notify_new_comment = 0;
		int notify_new_prise = 0;

		for (INotify<?> notify : owner.userNotifys().all()) {
			Notify.Type t = (Notify.Type) notify.type();
			if (t == Type.NEW_COMMENT) {
				NewCommentNotify ncNotify = (NewCommentNotify)notify;
				Assert.assertEquals(ncNotify.getIMessage(), msg);
				Assert.assertTrue(users.contains(ncNotify.getIComment().user()));
				notify_new_comment = notify_new_comment + 1;
			} else if (t == Type.NEW_PRISE) {
				NewPriseNotify npNotify = (NewPriseNotify)notify;
				Assert.assertEquals(npNotify.getIMessage(), msg);
				Assert.assertTrue(users.contains(npNotify.getIUser()));
				notify_new_prise = notify_new_prise + 1;
			}
		}

		Assert.assertEquals(notify_new_comment, user_num);
		Assert.assertEquals(notify_new_prise, user_num);

	}

}
