package com.mm.account.ems;

import java.util.Objects;

public class PojoEms implements IEms {

	
	String _code;
	String _phonenum;
	EMS_TYPE _type;
	
	
	@Override
	public String code() {
		return _code;
	}

	@Override
	public String phonenum() {
		return _phonenum;
	}

	@Override
	public EMS_TYPE type() {
		return _type;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof PojoEms)
		{
			PojoEms p_obj = (PojoEms)obj;
			return Objects.equals(_code, p_obj._code) && 
					Objects.equals(_phonenum, p_obj._phonenum) &&
					Objects.equals(_type, p_obj._type);
		}
		return false;
	}
	
}
