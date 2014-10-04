package com.mm.tinylove;

public interface IComment extends IObject{

	IMessage msg();
	IUser user();
	IRangeList<IUser> prisers();
	
	String content();
}
