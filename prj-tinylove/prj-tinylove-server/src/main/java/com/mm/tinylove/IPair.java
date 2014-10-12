package com.mm.tinylove;


public interface IPair extends IFollowObject{

	String name();
	IRangeList<IUser> user();
	IUser creator();
}
