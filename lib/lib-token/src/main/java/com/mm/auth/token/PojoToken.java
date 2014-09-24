package com.mm.auth.token;


abstract public class PojoToken implements IToken {

	
	String _id;
	String _token;
	int _duration; //second
	
	@Override
	public String id() {
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
