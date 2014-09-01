package com.mm.account.token;

public interface ITokenService {

	IToken newToken(long id);
	
	boolean checkValid(IToken token);
	
	void expireToken(IToken token);
	
}
