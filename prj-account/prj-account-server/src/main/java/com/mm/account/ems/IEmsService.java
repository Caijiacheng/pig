package com.mm.account.ems;

public interface IEmsService {
	public boolean checkEmsVaild(IEms ems);
	public IEms getEms(String phone);
}
