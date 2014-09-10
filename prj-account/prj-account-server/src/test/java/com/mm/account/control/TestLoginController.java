package com.mm.account.control;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.annotations.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mm.account.control.LoginController.ErrorRet;
import com.mm.account.control.LoginController.PasswdAuthCode;
import com.mm.account.control.LoginController.RegUserInfo;
import com.mm.account.control.LoginController.Token_Data;
import com.mm.account.control.LoginController.UserInfo;
import com.mm.account.control.LoginController.UserUpdatePwd;
import com.mm.account.ems.IEms;

public class TestLoginController {

	static Logger LOG = LoggerFactory.getLogger(TestLoginController.class);

	static Request buildRequest(HttpMethod method, String uri,
			Map<String, String> headers, String body) {
		ByteBuf content;

		if (body == null) {
			content = Unpooled.buffer(0);
		} else {
			content = Unpooled.wrappedBuffer(body.getBytes(Charsets.UTF_8));
		}

		FullHttpRequest r = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
				method, uri, content, false);

		if (headers != null)
			for (Entry<String, String> ent : headers.entrySet()) {
				r.headers().set(ent.getKey(), ent.getValue());
			}

		Request rr = new Request(r, null);
		return rr;

	}

	static <T> Request buildRequestFromAnnotation(Class<T> cls, String method,
			Map<String, String> headers, String body) {
		try {
			RequestMethod rm = cls.getMethod(method, Request.class,
					Response.class).getAnnotation(RequestMethod.class);
			Preconditions.checkNotNull(rm);
			return buildRequest(HttpMethod.valueOf(rm.method()), rm.value(),
					headers, body);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}

	}

	static String getControlMethodUri(Method m) {
		RequestMethod rm = m.getAnnotation(RequestMethod.class);
		Preconditions.checkNotNull(rm);
		return rm.value();
	}

	static HttpMethod getControlMethodHttpM(Method m) {
		RequestMethod rm = m.getAnnotation(RequestMethod.class);
		Preconditions.checkNotNull(rm);
		return HttpMethod.valueOf(rm.method());
	}

	static Response buildResponse() {
		return null;
	}

	static String[] s_test_phone_num = new String[] { "123456789", "223456789",
			"323456789", "423456789", "523456789", "623456789", "723456789",
			"823456789", "923456789", };

	
	static String getRandomPhoneNum()
	{
		return s_test_phone_num[ThreadLocalRandom.current().nextInt(s_test_phone_num.length)];
	}
	
	@Before
	public void setup() {
		LoginController lc = new LoginController();
		for (String s : s_test_phone_num) {
			lc.removeAccountForTestWithPhoneNum(s);
		}
	}

	static LoginController login = new LoginController();

	String getRegAuthCode(String phonenum, String type) {
		Map<String, String> headers = ImmutableMap.of("phoneNumber", phonenum,
				"type", type);
		Request r = buildRequestFromAnnotation(LoginController.class,
				"getEMSCode", headers, null);

		Assert.assertEquals(ErrorRet.SUCESS(), login.getEMSCode(r, null));

		return login.getEMSForTest(phonenum, type).code();
	}

	@Test
	public void testAccountOK() {

		Request r = buildRequestFromAnnotation(LoginController.class,
				"checkService", null, null);

		String ret = login.checkService(r, null);
		Assert.assertTrue(ret.contains("\"errorCode\":\"0\""));
	}

	@Test
	public void testCheckAuthCode() {
		String phonenum_reg = s_test_phone_num[0];
		String authcode_reg = getRegAuthCode(phonenum_reg, LoginController.TYPE_EMS_REG);
		Map<String, String> headers_reg = ImmutableMap.of("type", LoginController.TYPE_EMS_REG,
				"phoneNumber", phonenum_reg, "authCode", authcode_reg);

		Request r_reg = buildRequestFromAnnotation(LoginController.class,
				"checkEMSOK", headers_reg, null);

		Assert.assertEquals(login.checkEMSOK(r_reg, null), ErrorRet.SUCESS());

		// reg
		String phonenum_get_pwd = s_test_phone_num[1];
		String authcode_pwd = getRegAuthCode(phonenum_get_pwd, "1");

		Map<String, String> headers_pwd = ImmutableMap.of("type", LoginController.TYPE_EMS_GET_PWD,
				"phoneNumber", phonenum_get_pwd, "authCode", authcode_pwd);

		Request r_pwd = buildRequestFromAnnotation(LoginController.class,
				"checkEMSOK", headers_pwd, null);

		Assert.assertEquals(login.checkEMSOK(r_pwd, null), ErrorRet.SUCESS());

		// error authcode
		Map<String, String> headers_error = ImmutableMap.of("type", LoginController.TYPE_EMS_REG,
				"phoneNumber", phonenum_reg, "authCode", "-1");
		Request r_err = buildRequestFromAnnotation(LoginController.class,
				"checkEMSOK", headers_error, null);
		Assert.assertTrue(login.checkEMSOK(r_err, null).contains("-1"));

	}

	@Test
	public void testRegister() {

		String phonenum = getRandomPhoneNum();

		// get authcode
		String authcode = getRegAuthCode(phonenum, LoginController.TYPE_EMS_REG);

		LoginController.RegUserInfo reginfo = new LoginController.RegUserInfo();

		
		reginfo.deviceId = "111";
		reginfo.deviceType = "ios";
		reginfo.password = "2222";
		reginfo.phoneNumber = phonenum;

		//error_auth
		reginfo.authCode = "-1";
		Request r_authcode_err = buildRequestFromAnnotation(
				LoginController.class, "register", null,
				new Gson().toJson(reginfo));
		String authcode_err_json = login.register(r_authcode_err, null);
		Assert.assertTrue(authcode_err_json, authcode_err_json.contains("51011"));
		
		//correct_register
		reginfo.authCode = authcode;
		Request r = buildRequestFromAnnotation(LoginController.class,
				"register", null, new Gson().toJson(reginfo));
		String ret_json = login.register(r, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));
		//dup_register
		String dup_reg_json = login.register(r, null);
		Assert.assertEquals(dup_reg_json, ErrorRet.ERROR(51007));
		
		
		
		//login with token
		Token_Data.Ret token_data = new Gson().fromJson(ret_json, Token_Data.Ret.class);
		LOG.error("ret_json:{}", ret_json);
		Map<String, String> token_map = ImmutableMap.of("token", token_data.data.token);
		
		Request r_login_token = buildRequestFromAnnotation(LoginController.class, 
				"loginWithToken", token_map, null);
		Assert.assertEquals(login.loginWithToken(r_login_token, null), 
				ErrorRet.SUCESS());
		
		Map<String, String> err_token_map = ImmutableMap.of("token", "-1");
		Request r_login_token_err = buildRequestFromAnnotation(LoginController.class, 
				"loginWithToken", err_token_map, null);
		Assert.assertEquals(login.loginWithToken(r_login_token_err, null), 
				ErrorRet.ERROR(51008));
		
		//logout
		Request r_logout = buildRequestFromAnnotation(LoginController.class, 
				"logout", null, String.format("{token:%s}", token_data.data.token));
		Assert.assertEquals(login.logout(r_logout, null), 
				ErrorRet.ERROR(20008));
		//re_login failed
		Assert.assertEquals(login.loginWithToken(r_login_token, null), 
				ErrorRet.ERROR(51008));
	}
	
	
	@Test
	public void testLoginAndUpdatePwd()
	{
		//login without  register
		UserInfo user_info = new UserInfo();
		user_info.username = getRandomPhoneNum();
		user_info.password = "111";
		user_info.deviceId = "222";
		user_info.deviceType = "ios";
		
		Request r_without_reg = buildRequestFromAnnotation(LoginController.class,
				"login", null, new Gson().toJson(user_info));
		Assert.assertEquals(login.login(r_without_reg, null), ErrorRet.ERROR(51006));
		
		// register
		String phonenum = getRandomPhoneNum();
		String authcode = getRegAuthCode(phonenum, LoginController.TYPE_EMS_REG);

		LoginController.RegUserInfo reginfo = new LoginController.RegUserInfo();

		reginfo.deviceId = "111";
		reginfo.deviceType = "ios";
		reginfo.password = "3333";
		reginfo.phoneNumber = phonenum;

		//correct_register
		reginfo.authCode = authcode;
		Request r = buildRequestFromAnnotation(LoginController.class,
				"register", null, new Gson().toJson(reginfo));
		String ret_json = login.register(r, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));

		//login with pwd
		user_info.username = reginfo.phoneNumber;
		user_info.password = reginfo.password;
		
		Request r_login_pwd = buildRequestFromAnnotation(LoginController.class,
				"login", null, new Gson().toJson(user_info));
		ret_json = login.login(r_login_pwd, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));
		
		//update passwd with ems with error phonenum
		String pwd_auth_code = 
				getRegAuthCode(user_info.username, 
						LoginController.TYPE_EMS_GET_PWD);
		PasswdAuthCode pwdAuthCode = new PasswdAuthCode();
		pwdAuthCode.phoneNumber = "99999999";
		pwdAuthCode.password = "111";
		pwdAuthCode.authCode = pwd_auth_code;
		Request r_update_ems_pwd = buildRequestFromAnnotation(LoginController.class,
				"updatePasswdWithEms", null, new Gson().toJson(pwdAuthCode));
		Assert.assertEquals(login.updatePasswdWithEms(r_update_ems_pwd, null), 
				ErrorRet.ERROR(51006));
		
		pwdAuthCode.phoneNumber = reginfo.phoneNumber;
		r_update_ems_pwd = buildRequestFromAnnotation(LoginController.class,
				"updatePasswdWithEms", null, new Gson().toJson(pwdAuthCode));
		//update ok
		Assert.assertEquals(login.updatePasswdWithEms(r_update_ems_pwd, null), 
				ErrorRet.SUCESS());
		
		//login failed with old pwd
//		user_info.password = "5677";
		Request r_login_failed = buildRequestFromAnnotation(LoginController.class,
				"login", null, new Gson().toJson(user_info));
		Assert.assertEquals(login.login(r_login_failed, null), ErrorRet.ERROR(51003));
		
		//login ok with new pwd
		user_info.password = pwdAuthCode.password;
		Request r_login_new_pwd = buildRequestFromAnnotation(LoginController.class,
				"login", null, new Gson().toJson(user_info));
		ret_json = login.login(r_login_new_pwd, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));
		
		//update pwd with token
		Token_Data.Ret token_data = 
				new Gson().fromJson(ret_json, Token_Data.Ret.class);
		Map<String, String> token_map = ImmutableMap.of("token", token_data.data.token);
		
		UserUpdatePwd userpwd = new UserUpdatePwd();
		userpwd.oldPassword = pwdAuthCode.password;
		userpwd.password = "ttt";
		userpwd.passwordConfirm = "ttt";
		
		Request r_user_update_pwd = buildRequestFromAnnotation(LoginController.class,
				"updatePasswd", token_map, new Gson().toJson(userpwd));
		
		Assert.assertEquals(login.updatePasswd(r_user_update_pwd, null), ErrorRet.SUCESS());
		
		
		//re-login OK
		user_info.password = userpwd.password;
		Request r_re_login = buildRequestFromAnnotation(LoginController.class,
				"login", null, new Gson().toJson(user_info));
		ret_json = login.login(r_re_login, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));
	}
	
	@Test
	public void testUpdateUserInfo()
	{
		
	}

}
