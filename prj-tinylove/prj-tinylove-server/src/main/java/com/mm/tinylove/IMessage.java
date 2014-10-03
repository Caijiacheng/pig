package com.mm.tinylove;


public interface IMessage extends IObject{

	IUser publisher();
	IPair pair();
	IRangeList<IComment> comment();
	IStory parent();
	ILocation location();
	
}
