package com.mm.account.instance;

import com.google.common.base.Optional;

abstract class AbsAccount implements IAccount, ILoad {

	
	protected long id;
	protected String name;
	protected Long phoneid;
	protected Long weiboid;
	protected Long qqid;
	
	
	@Override
	public long id() {
		return id;
	}

	@Override
	public Optional<String> name() {
		return Optional.of(name);
	}

	@Override
	public Optional<Long> phoneid() {
		return Optional.of(phoneid);
	}

	@Override
	public Optional<Long> weiboid() {
		return Optional.of(weiboid);
	}

	@Override
	public Optional<Long> qqid() {
		return Optional.of(qqid);
	}


}
