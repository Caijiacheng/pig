package com.mm.account.ems;

import com.mm.account.server.IService;

public interface IEmsService extends IService{
	public boolean checkEmsVaild(IEms ems);
	public boolean checkEmsVaild(String phone, String authcode, IEms.EMS_TYPE type);
	public IEms getEms(String phone, IEms.EMS_TYPE type);
	
}
