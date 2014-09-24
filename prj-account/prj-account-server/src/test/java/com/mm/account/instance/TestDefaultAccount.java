package com.mm.account.instance;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Optional;
import com.mm.account.error.DupRegException;
import com.mm.account.error.NotExistException;
import com.mm.account.proto.Account.UserData;
import com.mm.account.proto.Account.UserData.Builder;

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
		
	}
	
	void removeIfExistPhoneid(IAccountService service, String phoneid)
	{
		Optional<IAccount> acc = service.getByPhoneId(phoneid);
		if (acc.isPresent())
		{
			service.unregister(acc.get().id());
		}
	}
	
	void removeIfExist3RDId(IAccountService service, String otherid, Platform3RD ptype)
	{
		Optional<IAccount> acc = service.getByPlatform3RD(otherid, ptype);
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
		Assert.assertNotNull(acc.requesttoken());
		Assert.assertTrue(service.exist(acc.id()));
		Assert.assertTrue(service.get(acc.id()).isPresent());
		Assert.assertEquals(service.get(acc.id()).get().phoneid().get(), phoneid);
		Assert.assertEquals(service.get(acc.id()).get().passwd().get(), pwd);		
		String modify_pwd = "1";
		service.modifyPasswd(acc.id(), modify_pwd);
		Assert.assertEquals(service.get(acc.id()).get().passwd().get(), modify_pwd);		
	}
	
	
	@Test
	public void testRegisterAccountWithQQ()
	{
		String qqid = "1223445";
		Platform3RD ptype = Platform3RD.QQ;
		String accessToken = "22";
		IAccountService service = new _DefautAccoutService();
		removeIfExist3RDId(service, qqid, ptype);
		IAccount acc = service.registerWith3RD(qqid, accessToken, ptype);
		Assert.assertEquals(acc.qqid().get(), qqid);
		Assert.assertEquals(acc.requesttoken(), accessToken);
		Assert.assertNotSame(service.rebuildToken(acc).requesttoken(), accessToken);
		
	}
	
	@Test
	public void testRegisterAccountWithWeibo()
	{
		String weiboid = "1223445";
		Platform3RD ptype = Platform3RD.WEIBO;
		String accessToken = "22";
		IAccountService service = new _DefautAccoutService();
		removeIfExist3RDId(service, weiboid, ptype);
		IAccount acc = service.registerWith3RD(weiboid, accessToken, ptype);
		Assert.assertEquals(acc.weiboid().get(), weiboid);
		Assert.assertEquals(acc.requesttoken(), accessToken);
		Assert.assertNotSame(service.rebuildToken(acc).requesttoken(), accessToken);
	}
	
	@Test
	public void testRegisterAccountWithWeixin()
	{
		String weixinid = "1223445";
		Platform3RD ptype = Platform3RD.WEIXIN;
		String accessToken = "22";
		IAccountService service = new _DefautAccoutService();
		removeIfExist3RDId(service, weixinid, ptype);
		IAccount acc = service.registerWith3RD(weixinid, accessToken, ptype);
		Assert.assertEquals(acc.weixinid().get(), weixinid);
		Assert.assertEquals(acc.requesttoken(), accessToken);
		Assert.assertNotSame(service.rebuildToken(acc).requesttoken(), accessToken);
	}
	
	@Deprecated
	@Ignore
	public void testAccoutIncrInfoVersion()
	{
		String phoneid = "1369926798222";
		String pwd = "2";
		IAccountService service = new _DefautAccoutService();
		removeIfExistPhoneid(service, phoneid);
		IAccount acc = service.register(phoneid, pwd);
		Assert.assertNotNull(acc);
		IAccount incr_acc = service.incrVersion(acc);
		Assert.assertEquals(acc.version() + 1, incr_acc.version());
		
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
	
	
	static class NetUserData
	{
		String a;
		String b;
		String c;
	}
	
	
	@Test
	public void testDefaultUserData0()
	{
		String phoneid = "1369926798222";
		String pwd = "2";
		IAccountService service = new _DefautAccoutService();
		removeIfExistPhoneid(service, phoneid);
		IAccount acc = service.register(phoneid, pwd);
		Assert.assertNotNull(acc);
		
		final NetUserData n_data = new NetUserData();
		n_data.a = "111";
		n_data.b = "222";
		n_data.c = "ccc";
		
		DefaultUserData0 userdata = new DefaultUserData0(acc) {
			
			@Override
			public  UserData.Builder transform(UserData.Builder builder) {
				return builder.setFirstName(n_data.a).setLastName(n_data.b).setHeadUrl(n_data.c);
				
			}
		};
		
		userdata.load();
		Assert.assertNotNull(userdata.data.getFirstName());
		Assert.assertEquals(userdata.data.getVersion(), 0);
		
		userdata.save();
		
		Assert.assertEquals(userdata.data.getFirstName(), n_data.a);
		Assert.assertEquals(userdata.data.getLastName(), n_data.b);
		Assert.assertEquals(userdata.current(), acc.version() + 1);
		
		//re get acc_count
		IAccount acc_1 = service.get(acc.id()).get();
		Assert.assertEquals(acc_1.version(), acc.version() + 1);

		DefaultUserData0 userdata_1 = new DefaultUserData0(acc_1) {
			@Override
			public Builder transform(Builder builder) {
				throw new UnsupportedOperationException();
			}
		};
		userdata_1.load();
		
		Assert.assertEquals(userdata_1.data.getFirstName(), n_data.a);
		Assert.assertEquals(userdata_1.data.getLastName(), n_data.b);
	}
	
	
	
	@Deprecated
	@Ignore
	public void testDefaultUserData()
	{
		String phoneid = "1369926798222";
		String pwd = "2";
		IAccountService service = new _DefautAccoutService();
		removeIfExistPhoneid(service, phoneid);
		IAccount acc = service.register(phoneid, pwd);
		Assert.assertNotNull(acc);
		
		final NetUserData n_data = new NetUserData();
		n_data.a = "111";
		n_data.b = "222";
		n_data.c = "ccc";
		
		DefaultUserData userdata = new DefaultUserData(acc) {
			
			@Override
			public  UserData.Builder transform(UserData.Builder builder) {
				return builder.setFirstName(n_data.a).setLastName(n_data.b).setHeadUrl(n_data.c);
				
			}
		};
		
		userdata.load();
		Assert.assertNotNull(userdata.data.getFirstName());
		Assert.assertEquals(userdata.data.getVersion(), 0);
		
		userdata.save();
		
		Assert.assertEquals(userdata.data.getFirstName(), n_data.a);
		Assert.assertEquals(userdata.data.getLastName(), n_data.b);
		Assert.assertEquals(userdata.current(), acc.version() + 1);
		
		//re get acc_count
		IAccount acc_1 = service.get(acc.id()).get();
		Assert.assertEquals(acc_1.version(), acc.version() + 1);

		DefaultUserData userdata_1 = new DefaultUserData(acc_1) {
			@Override
			public Builder transform(Builder builder) {
				throw new UnsupportedOperationException();
			}
		};
		
		userdata_1.load();
		
		Assert.assertEquals(userdata_1.data.getFirstName(), n_data.a);
		Assert.assertEquals(userdata_1.data.getLastName(), n_data.b);
	}
	
}



