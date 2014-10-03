package com.mm.tinylove;


public interface IUser extends IObject{

	String name();
	
	IRangeList<IStory> userStorys();
	IRangeList<IComment> userComments();
	IRangeList<IPair> userPairs();
//	@Deprecated
//	IRangeList<IPrise> userPrises();
	
	IRangeList<IMessage> msgPrise();
	IRangeList<IComment> commentPrise();

	
	IMessage publishMsg(IPair pair, String content, ILocation location, String imgurl, String videourl);
	IComment publishComment(IMessage msg, String content);
	
	void publishPrise(IMessage msg);
	void publishPriseOfComment(IComment comment);
	
//	IPrise publishPrise(IMessage msg);
//	IPrise publishPriseOfComment(IComment comment);
	
	
	
}
