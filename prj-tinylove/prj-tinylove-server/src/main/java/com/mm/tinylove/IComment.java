package com.mm.tinylove;

public interface IComment extends IObject{

	IMessage parent();
	IUser user();
	IRangeList<IUser> prisers();
}
