package com.mm.tinylove;


public interface IUser extends IObject{

	String name();
	
	IRangeList<IStory> userStorys();
	IRangeList<IComment> userComment();
	IRangeList<IPair> userPairs();
	
	IMessage publishMsg(IPair pair, String content, ILocation location, String imgurl, String videourl);
	IComment publishComment(IMessage msg, String content);
	IPrise publishPrise(IMessage msg);
	IPrise publishPriseOfComment(IComment comment);
	
	
	
}
