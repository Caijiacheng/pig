package com.mm.account.instance;

import com.google.common.base.Optional;

abstract class AbsAccount implements IAccount, ILoad {

	
	 long _id;
	 String _name;
	 String _phoneid;
	 String _weiboid;
	 String _qqid;
	 Integer _infover;  
	
	@Override
	public long id() {
		return _id;
	}
	
	@Override
	public int version() {
		return _infover;
	}

	@Override
	public Optional<String> name() {
		return Optional.of(_name);
	}

	@Override
	public Optional<String> phoneid() {
		return Optional.of(_phoneid);
	}

	@Override
	public Optional<String> weiboid() {
		return Optional.of(_weiboid);
	}

	@Override
	public Optional<String> qqid() {
		return Optional.of(_qqid);
	}


}
