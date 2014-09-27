package com.mm.tinylove;

import java.util.List;

public interface IMessage extends IObject{

	IUser publisher();
	IPair pair();
	List<IComment> comment();
	IStory parent();
	ILocation location();
	
}
