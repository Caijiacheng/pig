package com.mm.account.instance;

import com.google.common.base.Optional;

abstract class PojoAccount implements IAccount, ILoad {
	
	 long _id;
	 String _name;
	 String _phoneid;
	 String _weiboid;
	 String _qqid;
	 String _weixinid;
	 String _pwd;
	 Integer _infover;  
	 boolean _validate;
	 String _request_token;
	
	@Override
	public long id() {
		return _id;
	}
	
	public boolean validate()
	{
		return _validate;
	}
	
	@Override
	public int version() {
		return _infover;
	}

	@Override
	public Optional<String> name() {
		return Optional.fromNullable(_name);
	}
	
	@Override
	public Optional<String> phoneid() {
		return Optional.fromNullable(_phoneid);
	}

	@Override
	public Optional<String> weiboid() {
		return Optional.fromNullable(_weiboid);
	}

	@Override
	public Optional<String> qqid() {
		return Optional.fromNullable(_qqid);
	}

	public Optional<String> weixinid()
	{
		return Optional.fromNullable(_weixinid);
	}
	
	
	@Override
	public String requesttoken()
	{
		return _request_token;
	}

	@Override
	public Optional<String> passwd() {
		return Optional.fromNullable(_pwd);
	}
	
}
