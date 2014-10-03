package com.mm.tinylove.imp;

import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IStory;
import com.mm.tinylove.IUser;


//TODO:
/*  优化方法:
 * 		1. 延后加载.可以通过代理的方式,让调用各个IStorage接口的时候,延后load数据.这样会更平滑
*/
public class Ins {

	static DefaultStorageService s_storage_service = new DefaultStorageService();
	static IStorageService getStorageService()
	{
		return s_storage_service;
	}
	
	static IUniqService getUniqService()
	{
		return s_storage_service;
	}
	
	static IRangeService<Long> getLongRangeService()
	{
		return s_storage_service;
	}
	
	static IUser getIUser(long id)
	{
		return Ins.getStorageService().load(
				new DefaultUser(id));
	}
	
	static IStory getIStory(long id)
	{
		return Ins.getStorageService().load(new DefaultStory(id));
	}
	
	static IComment getIComment(long id)
	{
		return Ins.getStorageService().load(new DefaultComment(id));
	}
	
	
	static IMessage getIMessage(long id)
	{
		return Ins.getStorageService().load(new DefaultMessage(id));
	}
	
	static IPair getIPair(long id)
	{
		return Ins.getStorageService().load(new DefaultPair(id));
	}
	
	
	
}
