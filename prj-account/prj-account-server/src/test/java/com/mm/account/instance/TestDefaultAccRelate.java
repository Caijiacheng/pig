package com.mm.account.instance;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class TestDefaultAccRelate {

	
	static String[] s_test_phone_num = new String[] { "123456789", "223456789",
		"323456789", "423456789", "523456789", "623456789", "723456789",
		"823456789", "923456789", };
	
	
	void removeIfExistPhoneid(IAccountService service, String phoneid)
	{
		Optional<IAccount> acc = service.getByPhoneId(phoneid);
		if (acc.isPresent())
		{
			service.unregister(acc.get().id());
		}
	}
	
	IAccountService service = 
			new DefaultAccount.Service();
	
	@Before
	public void setup()
	{
		for (String s : s_test_phone_num)
		{
			removeIfExistPhoneid(service, s);
			service.register(s, "111");
		}
	}
	
	@After
	public void teardown()
	{
	}
	
	
	@Test
	public void testMakePairList()
	{
		for (String phone_a : s_test_phone_num)
		{
			for (String phone_b : s_test_phone_num)
			{
				if (!phone_a.equals(phone_b))
				{
					IAccount acc1 = 
							service.getByPhoneId(phone_a).get();
					IAccount acc2 = 
							service.getByPhoneId(phone_b).get();
					service.makePair(acc1, acc2);
				}
			}
		}
		
		
		for (String phone_a : s_test_phone_num)
		{
			for (String phone_b : s_test_phone_num)
			{
				if (!phone_a.equals(phone_b))
				{
					IAccount acc1 = 
							service.getByPhoneId(phone_a).get();
					IAccount acc2 = 
							service.getByPhoneId(phone_b).get();
					Assert.assertTrue(service.isPair(acc1, acc2));
					Assert.assertEquals(service.getPairsList(acc1).size(), s_test_phone_num.length - 1);
				}
			}
		}
		
	}
	
	@Test
	public void testOneByOneMakePair()
	{
		String phone1 = s_test_phone_num[0];
		String phone2 = s_test_phone_num[1];
	
		IAccount acc1 = 
				service.getByPhoneId(phone1).get();
		IAccount acc2 = 
				service.getByPhoneId(phone2).get();
		
		Assert.assertFalse(service.isPair(acc1, acc2));
		Assert.assertNull(service.getPairAskMsg(acc1, acc2).orNull());
		Assert.assertNull(service.getPairAskMsg(acc2, acc1).orNull());
		Assert.assertEquals(service.getPairsList(acc1).size(), 0);
		
		
		//make pair
		String a_ask_b = "hello, i am a";
		String b_ask_a = "do accept";
		
		Assert.assertFalse(service.makePair(acc1, acc2, a_ask_b));
		Assert.assertTrue(service.makePair(acc2, acc1, b_ask_a));
		
		Assert.assertTrue(service.isPair(acc1, acc2));
		Assert.assertEquals(a_ask_b, service.getPairAskMsg(acc1, acc2).orNull());
		Assert.assertEquals(b_ask_a, service.getPairAskMsg(acc2, acc1).orNull());
		
		Assert.assertEquals(service.getPairsList(acc1).size(), 1);
		Assert.assertEquals(service.getPairsList(acc2).size(), 1);
		
		//un pair
		service.unPair(acc1, acc2);
		
		Assert.assertFalse(service.isPair(acc1, acc2));
		Assert.assertNull(service.getPairAskMsg(acc1, acc2).orNull());
		Assert.assertNull(service.getPairAskMsg(acc2, acc1).orNull());
		Assert.assertEquals(service.getPairsList(acc1).size(), 0);
	}
}
