package com.mm.account.instance;

import com.google.common.base.Optional;

public interface IAccountService {
	IAccount register();
	
	Optional<IAccount> get(long id);
	
}
