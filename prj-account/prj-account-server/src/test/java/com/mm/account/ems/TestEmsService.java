package com.mm.account.ems;

import junit.framework.Assert;

import org.junit.Test;

public class TestEmsService {
	@Test
	public void testEmsService()
	{
		String phonenum1 = "123";
		String phonenum2 = "234";
		
		MockEmsService service = new MockEmsService();
		IEms ems = service.getEms(phonenum1);
		Assert.assertEquals(ems.phonenum(), phonenum1);
		Assert.assertTrue(service.checkEmsVaild(ems));
		
		IEms ems1 = service.getEms(phonenum2);
		Assert.assertEquals(ems1.phonenum(), phonenum2);
	}
	
	@Test
	public void testEmsExpire() throws InterruptedException
	{
		String phonenum1 = "123456";
		
		CacheEmsService.DURATION_EMS = 1;
		
		MockEmsService service = new MockEmsService();
		IEms ems = service.getEms(phonenum1);
		Assert.assertEquals(ems.phonenum(), phonenum1);
		Assert.assertTrue(service.checkEmsVaild(ems));
		Thread.sleep(2000);
		
		Assert.assertFalse(service.checkEmsVaild(ems));
		
		IEms ems1 = service.getEms(phonenum1);
		Assert.assertTrue(service.checkEmsVaild(ems1));
		Assert.assertNotSame(ems.code(), ems1.code());
	}
}
