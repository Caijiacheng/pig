package com.mm.account.login;

import com.mm.account.token.IToken;

public interface ILoginService {

	
	
	
	void loginWithToken(IToken token);
	
	IToken loginWithPwd(String userid, String passwd);
	
	void expireLogin(IToken token);
	
}
