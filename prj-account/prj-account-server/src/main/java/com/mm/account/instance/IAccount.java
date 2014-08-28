package com.mm.account.instance;

import com.google.common.base.Optional;

public interface IAccount {
	
	long id();
	
	Optional<String> name();
	
	Optional<Long> phoneid();
	
	Optional<Long> weiboid();
	
	Optional<Long> qqid(); 
	
	
}
