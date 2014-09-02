package com.mm.account.token;

import com.mm.account.server.IService;

public interface ITokenService extends IService{

	IToken newToken(long id);
	
	boolean checkValid(IToken token);
	
	void expireToken(IToken token);

	boolean checkValid(String token);//not userid is OK?
	void expireToken(String token);
}
