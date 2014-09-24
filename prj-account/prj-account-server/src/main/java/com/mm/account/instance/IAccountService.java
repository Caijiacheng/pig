package com.mm.account.instance;

import java.util.Collection;

import com.google.common.base.Optional;
import com.mm.account.proto.Account.UserRelate;
import com.mm.account.server.IService;

public interface IAccountService extends IService{
	
	IAccount register(String phoneid, String pwdmd5);
	
	void modifyPasswd(long userid, String pwdmd5);
	void unregister(long userid);
	
	Optional<IAccount> get(long id);
	
	
	IAccount registerWith3RD(String otherid, String accessToken, Platform3RD ptype);
	Optional<IAccount> getByPlatform3RD(String phoneid, Platform3RD ptype);
	Optional<IAccount> getByPhoneId(String phoneid);
//	Optional<IAccount> getByWeiboId(String weiboid);
//	Optional<IAccount> getByQQId(String qqid);
	
	boolean exist(long id);
	
	IAccount rebuildToken(IAccount acc);
	
	@Deprecated
	Collection<IAccount> getByPhoneId(Collection<String> phones);
	@Deprecated
	IAccount incrVersion(IAccount acc);
	
	@Deprecated
	Optional<String> getPairAskMsg(IAccount acc_from, IAccount acc_to);
	
	@Deprecated
	boolean makePair(IAccount acc_from, IAccount acc_to, String ask_msg);
	
	@Deprecated
	boolean makePair(IAccount acc_from, IAccount acc_to);
	
	@Deprecated
	boolean isPair(IAccount acc_a, IAccount acc_b);
	
	@Deprecated
	void unPair(IAccount acc_a, IAccount acc_b);
	
	@Deprecated
	Collection<IAccount> getPairsList(IAccount acc);
	
	@Deprecated
	Collection<UserRelate> getPairRelate(IAccount acc_from);
	
}
