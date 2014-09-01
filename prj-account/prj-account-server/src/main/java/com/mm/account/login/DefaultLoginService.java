package com.mm.account.login;

import com.mm.account.token.IToken;

public class DefaultLoginService implements ILoginService {
	
//	IEmsService _ems_service = new 
	

	@Override
	public void loginWithToken(IToken token) {

	}

	@Override
	public IToken loginWithPwd(String userid, String passwd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void expireLogin(IToken token) {

	}

}
