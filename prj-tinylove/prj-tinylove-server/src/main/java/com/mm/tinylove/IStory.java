package com.mm.tinylove;


public interface IStory extends IObject{

	IRangeList<IMessage> message();
	
	IPair pair();
}
