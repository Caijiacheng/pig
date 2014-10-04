package com.mm.tinylove.imp;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.mm.tinylove.IComment;
import com.mm.tinylove.ILocation;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Location;

public class TestDefaultUser {

	
	@Before
	public void setup()
	{
		Ins.s_storage_service = new RemoveStorageService();
		Ins.getStorageService().cleanStorage();
	}
	
	
	@Test
	public void testUserCheckConsist()
	{
		String content = "i love it";
		String comment = "it love you";
		ILocation location = new DefaultLocation(Location.newBuilder().setX(1.0f).setY(2.0f).build());
		
		IUser user = UserStorage.creatAndSave();
		
		IPair pair = user.createPair("tt");
		String imgurl = "http://www.hello.img.com";
		
		IMessage imsg = user.publishMsg(pair, content, location, imgurl, null);
		IComment icomment = user.publishComment(imsg, comment);
		user.publishPrise(imsg);
		user.publishPriseOfComment(icomment);
		//dup publish
		user.publishPrise(imsg);
		user.publishPriseOfComment(icomment);
		
		Assert.assertEquals(user.userPairs().size(), 1);
		Assert.assertEquals(user.userPairs().all().get(0), pair);
		Assert.assertEquals(user.userPairs().all().get(0).name(), "tt");

		Assert.assertEquals(user.userStorys().size(), 1);
		Assert.assertEquals(user.userStorys().all().get(0).message().size(), 1);
		IMessage iimsg = user.userStorys().all().get(0).message().all().get(0);
		Assert.assertEquals(iimsg, imsg);
		Assert.assertEquals(user, iimsg.publisher());
		
		Assert.assertEquals(iimsg.content(), content);
		Assert.assertEquals(iimsg.imgurl(), imgurl);
		Assert.assertEquals(iimsg.videourl(), "");
		
		Assert.assertEquals(iimsg.location(), location);
		Assert.assertEquals(iimsg.prisers().size(), 1);
		Assert.assertEquals(iimsg.prisers().all().get(0), user);

		IComment iicomment = iimsg.comments().all().get(0);
		Assert.assertEquals(iicomment, icomment);
		Assert.assertEquals(iicomment.content(), comment);
		
		Assert.assertEquals(iicomment.user(), user);
		Assert.assertEquals(iicomment.msg(), iimsg);
		Assert.assertEquals(iicomment.prisers().all().size(), 1);
		Assert.assertEquals(iicomment.prisers().all().get(0), user);
	}
	
	@Test
	public void testBuildStory()
	{
		String content = "i love it";
		String imgurl = "http://www.hello.img.com";
		ILocation location = new DefaultLocation(Location.newBuilder().setX(1.0f).setY(2.0f).build());
		IUser user = UserStorage.creatAndSave();
		IPair pair = user.createPair("tt");
		IPair pair1 = user.createPair("tt1");
	
		int message_num = 100;
		
		for (int i=0; i<message_num; i++)
		{
			user.publishMsg(pair, content + ":" + i, location, imgurl, null);
			user.publishMsg(pair1, content + ":" + i, location, imgurl, null);
		}
		
		Assert.assertEquals(user.userPairs().size(), 2);

		Assert.assertEquals(user.userStorys().size(), 2);
		Assert.assertEquals(user.userStorys().all().get(0).message().size(), message_num);
		Assert.assertEquals(user.userStorys().all().get(1).message().size(), message_num);
	
		//only to load all message to check db is ok
		user.userStorys().all().get(0).message().all().contains("aaa");
		user.userStorys().all().get(1).message().all().contains("aaa");
	}
	
	@Test
	public void testPublishComments()
	{
		String content = "i love it";
		String imgurl = "http://www.hello.img.com";
		ILocation location = new DefaultLocation(Location.newBuilder().setX(1.0f).setY(2.0f).build());
		IUser user = UserStorage.creatAndSave();
		IPair pair = user.createPair("tt");
		IMessage msg = user.publishMsg(pair, content, location, imgurl, null);
		int user_num = 100;
		
		for (int i=0; i<user_num; i++)
		{
			IUser user1 = UserStorage.creatAndSave();
			IComment comment = user1.publishComment(msg, "llll oooo vvvv ittttt:" + i);
			user1.publishPrise(msg);
			user.publishPriseOfComment(comment);
		}
		
		
		Assert.assertEquals(msg.comments().size(), user_num);
		Assert.assertEquals(msg.prisers().size(), user_num);
		Assert.assertEquals(user.commentPrise().size(), user_num);
		
		//only to load all user to check db is ok
		msg.prisers().all().contains(user);
		msg.comments().all().contains("1");
		
	}
}
