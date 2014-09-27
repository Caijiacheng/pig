package com.mm.tinylove;

import java.util.List;

public interface IUser extends IObject{

	String name();
	
	List<IStory> userStorys();
	List<IComment> userComment();
	List<IPair> userPairs();
	
	IMessage publishMsg(IPair pair, String content, String imgurl, String videourl);
	IComment publishComment(IMessage msg);
	IPrise publishPrise(IMessage msg);
	IPrise publishPriseOfComment(IComment comment);
	
	
	
}
