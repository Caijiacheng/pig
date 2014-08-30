package com.mm.account.instance;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Optional;
import com.mm.account.db.MysqlDB;
import com.mm.account.error.DBException;
import com.mm.account.error.DupRegException;
import com.mm.account.error.NotExistException;

public class TestDefaultAccount {

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
	
	static public class _DefautAccoutService extends DefaultAccount.Service
	{
		@Override
		public void unregister(long id) {
			String query = 
					String.format("delete from %s where id=%s", DefaultAccount.TABLE_NAME, id);
			
			MysqlDB db = new MysqlDB(DefaultAccount.DB_NAME);
			
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					stmt.execute(query);
				} 
			}catch (SQLException e) {
				throw new DBException(e);
			}	
		}
	}
	
	void removeIfExistPhoneid(IAccountService service, String phoneid)
	{
		Optional<IAccount> acc = service.getByPhoneId(phoneid);
		if (acc.isPresent())
		{
			service.unregister(acc.get().id());
		}
	}
	
	@Test
	public void testRegisterAccountWithPhoneid() {
		String phoneid = "13699267982";
		String pwd = "2";
		IAccountService service = new _DefautAccoutService();
		removeIfExistPhoneid(service, phoneid);
		IAccount acc = service.register(phoneid, pwd);
		Assert.assertNotNull(acc);
		Assert.assertTrue(service.exist(acc.id()));
		Assert.assertTrue(service.get(acc.id()).isPresent());
		Assert.assertEquals(service.get(acc.id()).get().phoneid().get(), phoneid);
		Assert.assertEquals(service.get(acc.id()).get().passwd(), pwd);		
		String modify_pwd = "1";
		service.modifyPasswd(acc.id(), modify_pwd);
		Assert.assertEquals(service.get(acc.id()).get().passwd(), modify_pwd);		
	}
	
	@Test(expected=NotExistException.class)
	public void testNotExistUseridInModifyPasswd()
	{
		String phoneid = "22221";
		String pwd = "111";
		IAccountService service = new _DefautAccoutService();
		removeIfExistPhoneid(service, phoneid);
		IAccount acc = service.register(phoneid, pwd);
		removeIfExistPhoneid(service, phoneid);
		service.modifyPasswd(acc.id(), "333");
	}
	
	@Test(expected=DupRegException.class)
	public void testRegisterDupFailedWithPhoneid()
	{
		String phoneid = "2222";
		String pwd = "3";
		IAccountService service = new _DefautAccoutService();
		removeIfExistPhoneid(service, phoneid);
		Assert.assertNotNull(service.register(phoneid, pwd));
		service.register(phoneid, pwd);
	}
}
