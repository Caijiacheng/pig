package com.mm.account.token;

import com.google.common.base.Optional;
import com.mm.account.server.IService;

public interface ITokenService extends IService{

	IToken newToken(long id);
	
	Optional<IToken> getToken(String token);
	
	boolean checkValid(IToken token);
	void expireToken(IToken token);

	
	
//	boolean checkValid(String token);//not userid is OK?
//	void expireToken(String token);
}
