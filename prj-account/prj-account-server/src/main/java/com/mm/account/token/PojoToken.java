package com.mm.account.token;


abstract public class PojoToken implements IToken {

	
	long _id;
	String _token;
	int _duration; //second
	
	@Override
	public long id() {
		return _id;
	}

	@Override
	public String token() {
		return _token;
	}

	@Override
	public int duration() {
		return _duration;
	}


}
