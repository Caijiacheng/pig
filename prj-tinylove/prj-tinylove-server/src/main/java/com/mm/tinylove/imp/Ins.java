package com.mm.tinylove.imp;

import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IStory;
import com.mm.tinylove.IUser;
import com.mm.tinylove.view.imp.EventTrigger;


//TODO:
/*  优化方法:
 * 		1. 延后加载.可以通过代理的方式,让调用各个IStorage接口的时候,延后load数据.这样会更平滑
*/
public class Ins {

	static DefaultStorageService s_storage_service = new DefaultStorageService();
	//static EventBus s_event_bus = new EventBus("DefaultDeventBus");
	static EventBus s_event_bus = new AsyncEventBus("DefautEventBus", 
			Executors.newCachedThreadPool());
	
	public static IStorageService getStorageService()
	{
		return s_storage_service;
	}
	
	
	public static IUniqService getUniqService()
	{
		return s_storage_service;
	}
	
	public static ICollectionService<Long> getLongRangeService()
	{
		return s_storage_service;
	}
	
	public static IUser getIUser(long id)
	{
		return Ins.getStorageService().load(
				new DefaultUser(id));
	}
	
	public static IStory getIStory(long id)
	{
		return Ins.getStorageService().load(new DefaultStory(id));
	}
	
	public static IComment getIComment(long id)
	{
		return Ins.getStorageService().load(new DefaultComment(id));
	}
	
	
	public static IMessage getIMessage(long id)
	{
		return Ins.getStorageService().load(new DefaultMessage(id));
	}
	
	public static IPair getIPair(long id)
	{
		return Ins.getStorageService().load(new DefaultPair(id));
	}
	
	public static EventBus getEventBus()
	{
		return s_event_bus;
	}
	static
	{
		EventTrigger et = new EventTrigger();
		Ins.getEventBus().register(et);
	}
	
}
