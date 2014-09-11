package com.mm.account.instance;

import com.google.common.base.Optional;

public interface IAccount {
	
	long id();
	
	Optional<String> name();
	
	Optional<String> phoneid();
	
	Optional<String> weiboid();
	
	Optional<String> qqid(); 
	
	int version();
	
	String passwd();
	
	boolean validate();
}
