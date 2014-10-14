package com.mm.tinylove.imp;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.mm.tinylove.ILocation;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Location;
import com.mm.tinylove.view.imp.MessageHotView;
import com.mm.tinylove.view.imp.MessageTSView;

public class TestMessageView {


	
	@Before
	public void setup()
	{
		Ins.s_storage_service = new RemoveStorageService();
		Ins.getStorageService().cleanStorage();
	}
	

	@Test
	public void testTSRank100() throws InterruptedException
	{
		String imgurl = "http://www.hello.img.com";
		ILocation location = new DefaultLocation(Location.newBuilder().setX(1.0f).setY(2.0f).build());
	
		int user_num = 150;
		
		Assert.assertTrue(user_num > 100);
		
		String content_prefix = "i love it ";
		for (int i=0; i<user_num; i++)
		{
			IUser user = UserStorage.createUserAndSave();
			String content = content_prefix + i;
			IPair pair = user.createPair("tt" + i);
			user.publishMsg(pair, content, location, imgurl, null);
		}

		Thread.sleep(1500);
		
		MessageTSView tsview = new MessageTSView();
		Assert.assertEquals(tsview.size(), user_num);
		List<IMessage> msgs = tsview.range(0, 100);
		
		for (int i=0; i<100; i++)
		{
			IMessage msg = msgs.get(i);
			Assert.assertEquals(msg.content(), content_prefix + (user_num-i-1));
			Assert.assertEquals(msg.pair().name(), "tt" + (user_num-i-1));
		}
	}
	
	@Test
	public void testHotRank30() throws InterruptedException
	{
		String imgurl = "http://www.hello.img.com";
		ILocation location = new DefaultLocation(Location.newBuilder().setX(1.0f).setY(2.0f).build());
	
		int user_num = 40;
		
		Assert.assertTrue(user_num > 30);
		
		String content_prefix = "i love it ";
		for (int i=0; i<user_num; i++)
		{
			IUser user = UserStorage.createUserAndSave();
			String content = content_prefix + i;
			IPair pair = user.createPair("tt" + i);
			user.publishMsg(pair, content, location, imgurl, null);
			
			for(IMessage msg : new MessageStorage().messageList().all())
			{
				user.publishComment(msg, "hehe");
				user.publishPrise(msg);
			}
		}

		Thread.sleep(1500);
		//越早加进去的message,被评论的次数越多
		MessageHotView tsview = new MessageHotView();
		Assert.assertEquals(tsview.size(), user_num);
		List<IMessage> msgs = tsview.range(0, 30);
		
		for (int i=0; i<30; i++)
		{
			IMessage msg = msgs.get(i);
			Assert.assertEquals(msg.content(), content_prefix + i);
			Assert.assertEquals(msg.pair().name(), "tt" + i);
		}
	}
}
