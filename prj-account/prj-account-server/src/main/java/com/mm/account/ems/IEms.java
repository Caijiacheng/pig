package com.mm.account.ems;

public interface IEms {
	String code();
	String phonenum();
	
	EMS_TYPE type();
	
	public enum EMS_TYPE
	{
		REG,
		GET_PWD,
	}
	
}
