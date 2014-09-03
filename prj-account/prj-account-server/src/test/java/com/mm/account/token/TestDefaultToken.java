package com.mm.account.token;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDefaultToken {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testToken()
	{
		ITokenService service = new DefaultToken.Service();
		
		IToken token = service.newToken(1);
		
		Assert.assertTrue(service.checkValid(token));
		
		service.expireToken(token);
		
		Assert.assertFalse(service.checkValid(token));
	}
	
	@Test
	public void testToken_1()
	{
		ITokenService service = new DefaultToken.Service();
		
		IToken token = service.newToken(134343);
		
		IToken token_0 = service.getToken(token.token()).get();
		
		Assert.assertTrue(service.checkValid(token_0));
		
		service.expireToken(token_0);
		
		Assert.assertFalse(service.checkValid(token_0));
	}
	
	@Test
	public void testTokenTimeExpire() throws InterruptedException
	{
		DefaultToken.Service.TOKEN_VAILD_PERIOD = 1;
		
		ITokenService service = new DefaultToken.Service();
		
		IToken token = service.newToken(2);
		Assert.assertTrue(service.checkValid(token));
		
		Thread.sleep((DefaultToken.Service.TOKEN_VAILD_PERIOD + 1)  * 1000);
		
		Assert.assertFalse(service.checkValid(token));
	}
	
	@Test
	public void testTokenExpiredSameID() throws InterruptedException
	{
		long userid = 12345;
		
		ITokenService service = new DefaultToken.Service();
		IToken token = service.newToken(userid);
		Assert.assertTrue(service.checkValid(token));
		
		IToken token_1 = service.newToken(userid);
		Assert.assertTrue(service.checkValid(token_1));
		Assert.assertFalse(service.checkValid(token));

	}

}
