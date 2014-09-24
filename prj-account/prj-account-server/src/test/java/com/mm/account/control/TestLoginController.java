package com.mm.account.control;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.mm.account.control.LoginController.ErrorCodeImgRet;
import com.mm.account.control.LoginController.ErrorRet;
import com.mm.account.control.LoginController.FrdIncrUpdate;
import com.mm.account.control.LoginController.FrdStatus;
import com.mm.account.control.LoginController.FrdStatusRet;
import com.mm.account.control.LoginController.FriendListRet;
import com.mm.account.control.LoginController.OneFrdStatuRet;
import com.mm.account.control.LoginController.PasswdAuthCode;
import com.mm.account.control.LoginController.Token_Data;
import com.mm.account.control.LoginController.UserDetailInfo;
import com.mm.account.control.LoginController.UserInfo;
import com.mm.account.control.LoginController.UserUpdatePwd;
import com.mm.account.control.LoginController.UsetInfoRet;


@Deprecated
@Ignore
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
			"823456789", "923456789", "34342423423434", "43434", "787454", "34435435400" };

	static String getRandomPhoneNum() {
		return s_test_phone_num[ThreadLocalRandom.current().nextInt(
				s_test_phone_num.length)];
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
		String authcode_reg = getRegAuthCode(phonenum_reg,
				LoginController.TYPE_EMS_REG);
		Map<String, String> headers_reg = ImmutableMap.of("type",
				LoginController.TYPE_EMS_REG, "phoneNumber", phonenum_reg,
				"authCode", authcode_reg);

		Request r_reg = buildRequestFromAnnotation(LoginController.class,
				"checkEMSOK", headers_reg, null);

		Assert.assertEquals(login.checkEMSOK(r_reg, null), ErrorRet.SUCESS());

		// reg
		String phonenum_get_pwd = s_test_phone_num[1];
		String authcode_pwd = getRegAuthCode(phonenum_get_pwd, "1");

		Map<String, String> headers_pwd = ImmutableMap.of("type",
				LoginController.TYPE_EMS_GET_PWD, "phoneNumber",
				phonenum_get_pwd, "authCode", authcode_pwd);

		Request r_pwd = buildRequestFromAnnotation(LoginController.class,
				"checkEMSOK", headers_pwd, null);

		Assert.assertEquals(login.checkEMSOK(r_pwd, null), ErrorRet.SUCESS());

		// error authcode
		Map<String, String> headers_error = ImmutableMap.of("type",
				LoginController.TYPE_EMS_REG, "phoneNumber", phonenum_reg,
				"authCode", "-1");
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

		// error_auth
		reginfo.authCode = "-1";
		Request r_authcode_err = buildRequestFromAnnotation(
				LoginController.class, "register", null,
				new Gson().toJson(reginfo));
		String authcode_err_json = login.register(r_authcode_err, null);
		Assert.assertTrue(authcode_err_json,
				authcode_err_json.contains("51011"));

		// correct_register
		reginfo.authCode = authcode;
		Request r = buildRequestFromAnnotation(LoginController.class,
				"register", null, new Gson().toJson(reginfo));
		String ret_json = login.register(r, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));
		// dup_register
		String dup_reg_json = login.register(r, null);
		Assert.assertEquals(dup_reg_json, ErrorRet.ERROR(51007));

		// login with token
		Token_Data.Ret token_data = new Gson().fromJson(ret_json,
				Token_Data.Ret.class);
		LOG.error("ret_json:{}", ret_json);
		Map<String, String> token_map = ImmutableMap.of("token",
				token_data.data.token);

		Request r_login_token = buildRequestFromAnnotation(
				LoginController.class, "loginWithToken", token_map, null);
		Assert.assertEquals(login.loginWithToken(r_login_token, null),
				ErrorRet.SUCESS());

		Map<String, String> err_token_map = ImmutableMap.of("token", "-1");
		Request r_login_token_err = buildRequestFromAnnotation(
				LoginController.class, "loginWithToken", err_token_map, null);
		Assert.assertEquals(login.loginWithToken(r_login_token_err, null),
				ErrorRet.ERROR(51008));

		// logout
		Request r_logout = buildRequestFromAnnotation(LoginController.class,
				"logout", null,
				String.format("{token:%s}", token_data.data.token));
		Assert.assertEquals(login.logout(r_logout, null), ErrorRet.ERROR(20008));
		// re_login failed
		Assert.assertEquals(login.loginWithToken(r_login_token, null),
				ErrorRet.ERROR(51008));
	}

	@Test
	public void testLoginAndUpdatePwd() {
		// login without register
		UserInfo user_info = new UserInfo();
		user_info.username = getRandomPhoneNum();
		user_info.password = "111";
		user_info.deviceId = "222";
		user_info.deviceType = "ios";

		Request r_without_reg = buildRequestFromAnnotation(
				LoginController.class, "login", null,
				new Gson().toJson(user_info));
		Assert.assertEquals(login.login(r_without_reg, null),
				ErrorRet.ERROR(51006));

		// register
		String phonenum = getRandomPhoneNum();
		String authcode = getRegAuthCode(phonenum, LoginController.TYPE_EMS_REG);

		LoginController.RegUserInfo reginfo = new LoginController.RegUserInfo();

		reginfo.deviceId = "111";
		reginfo.deviceType = "ios";
		reginfo.password = "3333";
		reginfo.phoneNumber = phonenum;

		// correct_register
		reginfo.authCode = authcode;
		Request r = buildRequestFromAnnotation(LoginController.class,
				"register", null, new Gson().toJson(reginfo));
		String ret_json = login.register(r, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));

		// login with pwd
		user_info.username = reginfo.phoneNumber;
		user_info.password = reginfo.password;

		Request r_login_pwd = buildRequestFromAnnotation(LoginController.class,
				"login", null, new Gson().toJson(user_info));
		ret_json = login.login(r_login_pwd, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));

		// update passwd with ems with error phonenum
		String pwd_auth_code = getRegAuthCode(user_info.username,
				LoginController.TYPE_EMS_GET_PWD);
		PasswdAuthCode pwdAuthCode = new PasswdAuthCode();
		pwdAuthCode.phoneNumber = "99999999";
		pwdAuthCode.password = "111";
		pwdAuthCode.authCode = pwd_auth_code;
		Request r_update_ems_pwd = buildRequestFromAnnotation(
				LoginController.class, "updatePasswdWithEms", null,
				new Gson().toJson(pwdAuthCode));
		Assert.assertEquals(login.updatePasswdWithEms(r_update_ems_pwd, null),
				ErrorRet.ERROR(51006));

		pwdAuthCode.phoneNumber = reginfo.phoneNumber;
		r_update_ems_pwd = buildRequestFromAnnotation(LoginController.class,
				"updatePasswdWithEms", null, new Gson().toJson(pwdAuthCode));
		// update ok
		Assert.assertEquals(login.updatePasswdWithEms(r_update_ems_pwd, null),
				ErrorRet.SUCESS());

		// login failed with old pwd
		// user_info.password = "5677";
		Request r_login_failed = buildRequestFromAnnotation(
				LoginController.class, "login", null,
				new Gson().toJson(user_info));
		Assert.assertEquals(login.login(r_login_failed, null),
				ErrorRet.ERROR(51003));

		// login ok with new pwd
		user_info.password = pwdAuthCode.password;
		Request r_login_new_pwd = buildRequestFromAnnotation(
				LoginController.class, "login", null,
				new Gson().toJson(user_info));
		ret_json = login.login(r_login_new_pwd, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));

		// update pwd with token
		Token_Data.Ret token_data = new Gson().fromJson(ret_json,
				Token_Data.Ret.class);
		Map<String, String> token_map = ImmutableMap.of("token",
				token_data.data.token);

		UserUpdatePwd userpwd = new UserUpdatePwd();
		userpwd.oldPassword = pwdAuthCode.password;
		userpwd.password = "ttt";
		userpwd.passwordConfirm = "ttt";

		Request r_user_update_pwd = buildRequestFromAnnotation(
				LoginController.class, "updatePasswd", token_map,
				new Gson().toJson(userpwd));

		Assert.assertEquals(login.updatePasswd(r_user_update_pwd, null),
				ErrorRet.SUCESS());

		// re-login OK
		user_info.password = userpwd.password;
		Request r_re_login = buildRequestFromAnnotation(LoginController.class,
				"login", null, new Gson().toJson(user_info));
		ret_json = login.login(r_re_login, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));
	}

	@Test
	public void testUpdateUserInfo() {
		// register
		String phonenum = getRandomPhoneNum();
		String authcode = getRegAuthCode(phonenum, LoginController.TYPE_EMS_REG);

		LoginController.RegUserInfo reginfo = new LoginController.RegUserInfo();

		reginfo.deviceId = "111";
		reginfo.deviceType = "ios";
		reginfo.password = "3333";
		reginfo.phoneNumber = phonenum;

		// correct_register
		reginfo.authCode = authcode;
		Request r = buildRequestFromAnnotation(LoginController.class,
				"register", null, new Gson().toJson(reginfo));
		String ret_json = login.register(r, null);
		Assert.assertTrue(ret_json, ret_json.contains("token"));

		// get token

		Token_Data.Ret token_data = new Gson().fromJson(ret_json,
				Token_Data.Ret.class);
		Map<String, String> token_map = ImmutableMap.of("token",
				token_data.data.token);

		// call getUserInfo
		Request r_get_user_info = buildRequestFromAnnotation(
				LoginController.class, "getUserinfo", token_map, null);
		ret_json = login.getUserinfo(r_get_user_info, null);

		Assert.assertTrue(ret_json, ret_json.contains("20001"));
		LOG.error("ret_json={}", ret_json);
		UsetInfoRet userinfo = new Gson().fromJson(ret_json, UsetInfoRet.class);
		UserDetailInfo user_detail = userinfo.data;
		Assert.assertNull(user_detail.firstName);
		Assert.assertNull(user_detail.gender);
		Assert.assertNull(user_detail.headUrl);

		// update userinfo
		user_detail.firstName = "hello";
		user_detail.lastName = "bbb";
		user_detail.gender = "1";

		Request r_update_user_info = buildRequestFromAnnotation(
				LoginController.class, "updateUserinfo", token_map,
				new Gson().toJson(user_detail));
		Assert.assertEquals(login.updateUserinfo(r_update_user_info, null),
				ErrorRet.ERROR(20001));

		// upload img
		Request r_upload_img = buildRequestFromAnnotation(
				LoginController.class, "uploadUserFaceImg", token_map,
				"img.bin");

		ret_json = login.uploadUserFaceImg(r_upload_img, null);

		ErrorCodeImgRet ret = new Gson().fromJson(ret_json,
				ErrorCodeImgRet.class);
		Assert.assertEquals(ret.errorCode, "0");

		// getUserInfo
		ret_json = login.getUserinfo(r_get_user_info, null);
		LOG.error("getUserInfo:{}", ret_json);
		Assert.assertTrue(ret_json, ret_json.contains("20001"));
		userinfo = new Gson().fromJson(ret_json, UsetInfoRet.class);
		UserDetailInfo user_detail_update = userinfo.data;
		Assert.assertEquals(user_detail_update.firstName, user_detail.firstName);
		Assert.assertEquals(user_detail_update.gender, user_detail.gender);
		Assert.assertEquals(user_detail_update.headUrl, ret.data.headUrl);
	}

	@Test
	public void testFriendRelate() {
		int reg_user_num = s_test_phone_num.length / 2;
		int un_reg_user_num = s_test_phone_num.length - reg_user_num;

		Token_Data.Ret token_data = null;
		String login_user_phone = null;

		Map<String, Token_Data.Ret> other_tokens = Maps.newHashMap();

		Assert.assertTrue(reg_user_num != 0);
		for (int i = 0; i < reg_user_num; i++) {
			String phonenum = s_test_phone_num[i];
			String authcode = getRegAuthCode(phonenum,
					LoginController.TYPE_EMS_REG);
			LoginController.RegUserInfo reginfo = new LoginController.RegUserInfo();

			reginfo.deviceId = "111";
			reginfo.deviceType = "ios";
			reginfo.password = "3333";
			reginfo.phoneNumber = phonenum;
			// correct_register
			reginfo.authCode = authcode;
			Request r = buildRequestFromAnnotation(LoginController.class,
					"register", null, new Gson().toJson(reginfo));
			String ret_json = login.register(r, null);
			Assert.assertTrue(ret_json, ret_json.contains("token"));

			if (login_user_phone == null) {
				login_user_phone = phonenum;
				token_data = new Gson()
						.fromJson(ret_json, Token_Data.Ret.class);
			} else {
				other_tokens.put(phonenum,
						new Gson().fromJson(ret_json, Token_Data.Ret.class));
			}
		}

		Map<String, String> token_map = ImmutableMap.of("token",
				token_data.data.token, "username",
				Joiner.on(",").join(s_test_phone_num));

		// getRelateStatus
		Request r_get_relate_status = buildRequestFromAnnotation(
				LoginController.class, "getRelateStatus", token_map, null);
		String ret_relate_statue = login.getRelateStatus(r_get_relate_status,
				null);

		FrdStatusRet ret = new Gson().fromJson(ret_relate_statue,
				FrdStatusRet.class);
		LOG.error("self_phone{}, userid:{}, ret_relate_statue:{}", 
				login_user_phone, token_data.data.userId, ret_relate_statue);
		Assert.assertEquals(ret.data.size(), s_test_phone_num.length - 1);

		int add_cnt = 0;
		int added_cnt = 0;
		int invite_cnt = 0;
		int validate_cnt = 0;
		List<Long> other_reg_ids = Lists.newArrayList();
		for (FrdStatusRet._data data : ret.data) {
			if (data.status == FrdStatus.add) {
				add_cnt++;
				other_reg_ids.add(data.friendid);
			} else if (data.status == FrdStatus.added) {
				added_cnt++;
			} else if (data.status == FrdStatus.invite) {
				invite_cnt++;
			} else if (data.status == FrdStatus.validate) {
				validate_cnt++;
			}
		}
		Assert.assertEquals(add_cnt, reg_user_num - 1);
		Assert.assertEquals(added_cnt, 0);
		Assert.assertEquals(validate_cnt, 0);
		Assert.assertEquals(invite_cnt, un_reg_user_num);

		//validateFriend
		for (Long o_id : other_reg_ids) {
			Map<String, String> validate_token_map = ImmutableMap.of("token",
					token_data.data.token, "friendId", o_id.toString(),
					"content", "i love you");
			Request r = buildRequestFromAnnotation(LoginController.class,
					"validateFriend", validate_token_map, null);
			Assert.assertEquals(login.validateFriend(r, null),
					ErrorRet.SUCESS());

		}

		ret_relate_statue = login.getRelateStatus(r_get_relate_status, null);

		ret = new Gson().fromJson(ret_relate_statue, FrdStatusRet.class);
		Assert.assertEquals(ret.data.size(), s_test_phone_num.length - 1);
		add_cnt = 0;
		added_cnt = 0;
		invite_cnt = 0;
		validate_cnt = 0;
		for (FrdStatusRet._data data : ret.data) {
			if (data.status == FrdStatus.add) {
				add_cnt++;
			} else if (data.status == FrdStatus.added) {
				added_cnt++;
			} else if (data.status == FrdStatus.invite) {
				invite_cnt++;
			} else if (data.status == FrdStatus.validate) {
				validate_cnt++;
			}
		}
		Assert.assertEquals(add_cnt, 0);
		Assert.assertEquals(added_cnt, 0);
		Assert.assertEquals(validate_cnt, reg_user_num - 1);
		Assert.assertEquals(invite_cnt, un_reg_user_num);
		//other reg_user accept the user
		for (Token_Data.Ret token_ret : other_tokens.values())
		{
			Map<String, String> accept_token_map = ImmutableMap.of(
					"token", token_ret.data.token, 
					"friendId", String.valueOf(token_data.data.userId));
			Request r_accept = buildRequestFromAnnotation(LoginController.class,
					"acceptFriend", accept_token_map, null);
			Assert.assertEquals(login.acceptFriend(r_accept, null),
					ErrorRet.SUCESS());
		}
		
		//confirm relate_status
		ret_relate_statue = login.getRelateStatus(r_get_relate_status, null);

		ret = new Gson().fromJson(ret_relate_statue, FrdStatusRet.class);
		Assert.assertEquals(ret.data.size(), s_test_phone_num.length - 1);
		add_cnt = 0;
		added_cnt = 0;
		invite_cnt = 0;
		validate_cnt = 0;
		for (FrdStatusRet._data data : ret.data) {
			if (data.status == FrdStatus.add) {
				add_cnt++;
			} else if (data.status == FrdStatus.added) {
				added_cnt++;
			} else if (data.status == FrdStatus.invite) {
				invite_cnt++;
			} else if (data.status == FrdStatus.validate) {
				validate_cnt++;
			}
		}
		Assert.assertEquals(add_cnt, 0);
		Assert.assertEquals(added_cnt, reg_user_num - 1);
		Assert.assertEquals(validate_cnt, 0);
		Assert.assertEquals(invite_cnt, un_reg_user_num);
		
		//getFriendList
		Map<String, String> token_get_frd_list = 
				ImmutableMap.of("token", token_data.data.token);
		Request r_get_frd_list = buildRequestFromAnnotation(
				LoginController.class, "getFriendList", token_get_frd_list, null);
		String frd_list_gson = login.getFriendList(r_get_frd_list, null);
		FriendListRet frd_list = new Gson().fromJson(frd_list_gson, FriendListRet.class);
		Assert.assertEquals(frd_list.data.size(), reg_user_num - 1);
		
		//searchFriend
		String one_frd_phone = Lists.newArrayList(other_tokens.keySet()).get(0);
		Map<String, String> token_search = 
				ImmutableMap.of("token", token_data.data.token, "username", one_frd_phone);
		Request r_search_frd = buildRequestFromAnnotation(
				LoginController.class, "searchFriend", token_search, null);
		String search_frd_ret = login.searchFriend(r_search_frd, null);
		OneFrdStatuRet one_frd_ret = new Gson().fromJson(search_frd_ret, OneFrdStatuRet.class);
		Assert.assertEquals(one_frd_ret.data.status, FrdStatus.added);
		
		//deleteFriend
		String del_frd_id = String.valueOf(one_frd_ret.data.friendid);
		Map<String,String> token_del_frd = 
				ImmutableMap.of("token", token_data.data.token, "friendId", del_frd_id);
		Request r_del_frd = buildRequestFromAnnotation(LoginController.class,
				"deleteFriend", token_del_frd, null);
		String del_frd_ret = login.deleteFriend(r_del_frd, null);
		Assert.assertEquals(del_frd_ret, ErrorRet.SUCESS());
		//re-search
		search_frd_ret = login.searchFriend(r_search_frd, null);
		one_frd_ret = new Gson().fromJson(search_frd_ret, OneFrdStatuRet.class);
		Assert.assertEquals(one_frd_ret.data.status, FrdStatus.add);	
		
		//increUpdate
		List<String> friend_incr_ids = Lists.newArrayList();
		List<String> friend_incr_vers = Lists.newArrayList();
		int skip_cnt = 1;
		for (Token_Data.Ret tr : other_tokens.values())
		{
			if (one_frd_ret.data.friendid != tr.data.userId)
			{
				if (skip_cnt -- > 0)
					continue;
			}
			friend_incr_ids.add(String.valueOf(tr.data.userId));
			friend_incr_vers.add("-1");
		}
		
		friend_incr_ids.add("-2324343");
		friend_incr_vers.add("23232");
		
		
		Map<String, String> token_incre_update = 
				ImmutableMap.of("token", token_data.data.token,
						"friendIds", Joiner.on(",").join(friend_incr_ids),
						"versionIds", Joiner.on(",").join(friend_incr_vers));
		Request r_incr_update = buildRequestFromAnnotation(LoginController.class, 
				"increUpdateFriend", token_incre_update, null);
		String ret_incr_update_json = login.increUpdateFriend(r_incr_update, null);
		FrdIncrUpdate update_info = new Gson().fromJson(ret_incr_update_json, 
				FrdIncrUpdate.class);
		LOG.error("ret_incr_update:{}", ret_incr_update_json);
		//i skip one friend
		Assert.assertEquals(update_info.data.add.size(), 1);
		//one frd skip, one delete
		Assert.assertEquals(update_info.data.edit.size(), other_tokens.size() - 1 - 1);
		//one has delete
		Assert.assertEquals(update_info.data.delete.size(), 2);

		
		
	}

}
