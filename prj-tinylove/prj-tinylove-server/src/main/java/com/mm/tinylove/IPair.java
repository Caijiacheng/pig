package com.mm.tinylove;


public interface IPair extends IObject{

	String name();
	IRangeList<IUser> user();
	IUser creator();
}
