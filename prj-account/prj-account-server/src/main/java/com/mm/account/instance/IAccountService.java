package com.mm.account.instance;

import com.google.common.base.Optional;

public interface IAccountService {
	IAccount register(String phoneid, String pwdmd5);
	
	void unregister(long userid);
	
	void modifyPasswd(long userid, String pwdmd5);
	
	Optional<IAccount> get(long id);
	Optional<IAccount> getByPhoneId(String phoneid);
	Optional<IAccount> getByWeiboId(String weiboid);
	Optional<IAccount> getByQQId(String qqid);
	
	boolean exist(long id);
	
	
}
