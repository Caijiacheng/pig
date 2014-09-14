package com.mm.account.instance;

import java.util.Collection;

import com.google.common.base.Optional;
import com.mm.account.proto.Account.UserRelate;
import com.mm.account.server.IService;

public interface IAccountService extends IService{
	IAccount register(String phoneid, String pwdmd5);
	
	void unregister(long userid);
	
	void modifyPasswd(long userid, String pwdmd5);
	
	Optional<IAccount> get(long id);
	Optional<IAccount> getByPhoneId(String phoneid);
	Collection<IAccount> getByPhoneId(Collection<String> phones);
	Optional<IAccount> getByWeiboId(String weiboid);
	Optional<IAccount> getByQQId(String qqid);
	
	boolean exist(long id);
	
	@Deprecated
	IAccount incrVersion(IAccount acc);
	
	Optional<String> getPairAskMsg(IAccount acc_from, IAccount acc_to);
	
	
	boolean makePair(IAccount acc_from, IAccount acc_to, String ask_msg);
	
	boolean makePair(IAccount acc_from, IAccount acc_to);
	
	boolean isPair(IAccount acc_a, IAccount acc_b);
	
	void unPair(IAccount acc_a, IAccount acc_b);
	
	Collection<IAccount> getPairsList(IAccount acc);
	
	Collection<UserRelate> getPairRelate(IAccount acc_from);
	
}
