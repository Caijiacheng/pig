package com.mm.tinylove;

public interface IUser extends IObject {

	String name();

	IRangeList<IStory> userStorys();

	IRangeList<IComment> userComments();

	IRangeList<IPair> userPairs();

	IRangeList<IMessage> msgPrised();

	IRangeList<IComment> commentPrise();

	IMessage publishMsg(IPair pair, String content, ILocation location,
			String imgurl, String videourl);

	IComment publishComment(IMessage msg, String content);

	void publishPrise(IMessage msg);

	void publishPriseOfComment(IComment comment);

	IPair createPair(String name);

	void follow(IFollowObject obj);

	void unfollow(IFollowObject obj);

	IRangeList<INotify<?>> userNotifys();

}
