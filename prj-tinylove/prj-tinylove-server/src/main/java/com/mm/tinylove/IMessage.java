package com.mm.tinylove;


public interface IMessage extends IFollowObject{

	IUser publisher();
	IPair pair();
	IRangeList<IComment> comments();
	IRangeList<IUser> prisers();
	IStory parent();
	ILocation location();
	String content();
	String imgurl();
	String videourl();
	long timestamp();
}
