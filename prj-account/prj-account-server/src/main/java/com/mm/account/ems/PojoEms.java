package com.mm.account.ems;

public class PojoEms implements IEms {

	
	String _code;
	String _phonenum;
	
	@Override
	public String code() {
		return _code;
	}

	@Override
	public String phonenum() {
		return _phonenum;
	}

}
