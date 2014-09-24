package com.mm.auth.token;

import com.google.common.base.Optional;

public interface ITokenService {

	IToken newToken(String uniqid);
	
	Optional<IToken> getToken(String token);
	
	boolean checkValid(IToken token);
	void expireToken(IToken token);
	
}
