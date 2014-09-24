package com.mm.account.instance;

import com.google.common.base.Optional;

public interface IAccount {
	long id();
	Optional<String> passwd();
	
	public boolean validate();
	
	public int version() ;

	public Optional<String> name() ;

	public Optional<String> phoneid() ;

	public Optional<String> weiboid() ;

	public Optional<String> qqid() ;

	public Optional<String> weixinid();
	
	public String requesttoken();

}
