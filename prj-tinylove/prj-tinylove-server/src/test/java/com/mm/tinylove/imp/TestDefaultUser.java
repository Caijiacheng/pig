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
		Assert.assertEquals(iicomment.prisers().all().get(0), user);
	
		
		
		
		
		
	}
	
}
