package com.mm.account.ems;

public interface IEmsFactory {
	void send(IEms ems);
	IEms newEms(String phonenum);
	
}
