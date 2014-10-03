package com.mm.tinylove;


public interface IMessage extends IObject{

	IUser publisher();
	IPair pair();
	IRangeList<IComment> comments();
	IRangeList<IUser> prisers();
//	IRangeList<IPrise> prises();
	IStory parent();
	ILocation location();
	
}
