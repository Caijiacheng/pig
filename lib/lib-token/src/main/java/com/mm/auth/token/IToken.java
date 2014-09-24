package com.mm.auth.token;


public interface IToken {

	String id();
	
	String token();
	
	int duration();
	
}
