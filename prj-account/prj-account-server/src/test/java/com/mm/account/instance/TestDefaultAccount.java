package com.mm.account.instance;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Optional;
import com.mm.account.db.MysqlDB;
import com.mm.account.error.DBException;

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
	public void testRegisterAccount() {
		String phoneid = "13699267982";
		IAccountService service = new _DefautAccoutService();
		removeIfExistPhoneid(service, phoneid);
		IAccount acc = service.register(phoneid);
		assertTrue(service.exist(acc.id()));
		assertTrue(service.get(acc.id()).isPresent());
	}

}
