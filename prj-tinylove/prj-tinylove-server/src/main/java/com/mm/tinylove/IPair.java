package com.mm.tinylove;

import java.util.List;

public interface IPair extends IObject{

	String name();
	List<IUser> user();
	IUser creator();
}
