package com.mm.tinylove;


public interface IPair extends IFollowObject{

	String name();
	IRandSet<IUser> user();
	IUser creator();
}
