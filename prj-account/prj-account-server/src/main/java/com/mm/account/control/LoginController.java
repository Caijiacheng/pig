package com.mm.account.control;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.annotations.RequestMethod;
import org.restexpress.exception.BadRequestException;
import org.restexpress.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mm.account.data.DefaultPhoto;
import com.mm.account.data.IPhoto;
import com.mm.account.data.IPhotoService;
import com.mm.account.data.IUrl;
import com.mm.account.ems.IEms;
import com.mm.account.ems.IEms.EMS_TYPE;
import com.mm.account.ems.IEmsService;
import com.mm.account.ems.MockEmsService;
import com.mm.account.error.AccountException;
import com.mm.account.error.DupRegException;
import com.mm.account.instance.DefaultAccount;
import com.mm.account.instance.DefaultUserData;
import com.mm.account.instance.IAccount;
import com.mm.account.instance.IAccountService;
import com.mm.account.proto.Account.Gender;
import com.mm.account.proto.Account.UserData.Builder;
import com.mm.account.token.DefaultToken;
import com.mm.account.token.IToken;
import com.mm.account.token.ITokenService;

public class LoginController {
	
	
	final static String TYPE_EMS_GET_PWD = "1";
	final static String TYPE_EMS_REG = "0";
	
	
	static Logger LOG = LoggerFactory.getLogger(LoginController.class);
	
	IAccountService acc_service = 
			new DefaultAccount.Service();
	
	IEmsService ems_service = 
			new MockEmsService();
	
	ITokenService token_service = 
			new DefaultToken.Service();
	
	
	static class ErrorRet
	{
		ErrorRet(String ec)
		{
			this.errorCode = ec;
		}
		
		ErrorRet(String ec, String emsg)
		{
			this.errorCode = ec;
			this.errorMsg = emsg;
		}
		
		String errorCode;
		String errorMsg;
		
		String toJson()
		{
			return new Gson().toJson(this);
		}
		
		static String SUCESS()
		{
			return new ErrorRet("0").toJson();
		}
		
		static String ERROR(int num)
		{
			return new ErrorRet(Integer.toString(num)).toJson();
		}
		
		static String ERROR(int num, String errmsg)
		{
			return new ErrorRet(Integer.toString(num), errmsg).toJson();
		}
	}
	
	@RequestMethod(value="/addressbook/rest/pass/register/JH_reg_send/{phoneNumber}/{type}",
			method="GET")
	public String getEMSCode(Request request, Response response)
	{
		String type = request.getHeader("type", "not param type ?");
		String phonenum = request.getHeader("phoneNumber", "not param phonenumber ?");
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(phonenum);
		
		try
		{
			if (type == TYPE_EMS_GET_PWD)
			{//reg
				ems_service.getEms(phonenum, IEms.EMS_TYPE.GET_PWD);
			}else if (type == TYPE_EMS_REG)
			{//get passwd
				ems_service.getEms(phonenum, IEms.EMS_TYPE.REG);
			}else
			{
				throw new BadRequestException("param type is error: " + type);
			}
		}catch(AccountException ex)
		{
			LOG.error("", ex);
			return ErrorRet.ERROR(7002);
		}
		
		return ErrorRet.SUCESS();
	}
	
	@RequestMethod(value="/addressbook/rest/pass/register/JH_reg_validate/{phoneNumber}/{authCode}/{type}",
			method="GET")
	public String checkEMSOK(Request request, Response response)
	{
		
		String type = request.getHeader("type", "not param type ?");
		String phonenum = request.getHeader("phoneNumber", "not param phonenumber ?");
		String authcode = request.getHeader("authCode", "not param authCode ?");

		IEms.EMS_TYPE ems_type;
		
		try
		{
			if (type == TYPE_EMS_REG)
			{//reg
				ems_type = IEms.EMS_TYPE.REG;
			}else if (type == TYPE_EMS_GET_PWD)
			{//get passwd
				ems_type = IEms.EMS_TYPE.GET_PWD;
			}else
			{
				throw new BadRequestException("param type is error: " + type);
			}
			
			if (ems_service.checkEmsVaild(phonenum, authcode, ems_type))
			{
				return ErrorRet.SUCESS();
			}else
			{
				return ErrorRet.ERROR(-1, "check Ems Vaild failed!");
			}
		}catch(AccountException ex)
		{
			LOG.error("", ex);
			return ErrorRet.ERROR(-1000, ex.getMessage());
		}
	}

	static public class RegUserInfo extends UserInfo
	{
		
		String phoneNumber;
		String authCode;
		static public RegUserInfo fromJson(String json)
		{
			return new Gson().fromJson(json, RegUserInfo.class);
		}
	}
	
	static class Token_Data
	{
		long userId;
		String token;
		
		static class Ret extends ErrorRet
		{
			Ret(long id, String token)
			{
				super("0");
				data.token = token;
				data.userId = id;
			}
			
			Token_Data data = new Token_Data();
			
			String toJson()
			{
				return new Gson().toJson(this);
			}
		}
	}
	
	
	void removeAccountForTestWithPhoneNum(String phone)
	{
		Optional<IAccount> acc = acc_service.getByPhoneId(phone);
		if ( !acc.isPresent() )
		{
			return;
		}
		
		acc_service.unregister(acc.get().id());
	}
	
	IEms getEMSForTest(String phone, IEms.EMS_TYPE type)
	{
		return ems_service.getEms(phone, type);
	}
	
	IEms getEMSForTest(String phone, String type)
	{
		IEms.EMS_TYPE ems_type;
		if (type == TYPE_EMS_REG)
		{
			ems_type = IEms.EMS_TYPE.REG;
		}else if(type == TYPE_EMS_GET_PWD)
		{
			ems_type = IEms.EMS_TYPE.GET_PWD;
		}else
		{
			throw new RuntimeException();
		}
		
		return ems_service.getEms(phone, ems_type);
	}
	
	@RequestMethod(value="/addressbook/rest/pass/register/JH_reg_save", 
			method="POST")
	public String register(Request request, Response response)
	{
		try
		{
			RegUserInfo info = 
					RegUserInfo.fromJson(
							request.getBody().toString(Charsets.UTF_8));
			
			Optional<IAccount> acc = acc_service.getByPhoneId(info.phoneNumber);
			if ( acc.isPresent() )
			{
				return ErrorRet.ERROR(51007);
			}
			
			if (!ems_service.checkEmsVaild(info.phoneNumber, info.authCode, EMS_TYPE.REG))
			{
				return ErrorRet.ERROR(51011, "check authcode failed");
			}
			
			IAccount reg_acc = acc_service.register(info.phoneNumber, info.password);
			IToken token = token_service.newToken(reg_acc.id());
			
			return new Token_Data.Ret(token.id(), token.token()).toJson();
			
		}catch(JsonSyntaxException e)
		{
			throw new BadRequestException(e);
		}catch(DupRegException e)
		{
			return ErrorRet.ERROR(51007);
		}catch(Throwable e)
		{
			throw new ServiceException(e);
		}
		
	}
	
	@RequestMethod(value="/addressbook/rest/pass/check/JH_check_system", 
			method="GET")
	public String checkService(Request request, Response response)
	{
		if (!ems_service.ping())
		{
			return ErrorRet.ERROR(-2, "EMS Service failed");
		}
		
		if (!acc_service.ping())
		{
			return ErrorRet.ERROR(-3, "Account Service failed");
		}
		
		if (!token_service.ping())
		{
			return ErrorRet.ERROR(-4, "Token Service failed");
		}
		
		return "{\"errorCode\":\"0\", \"data\":\"isOK\"}"; //why need data?
		
	}

	
	static public class UserInfo
	{
		String username;
		String password;
		String deviceId;
		String deviceType;
	}
	
	@RequestMethod(value="/addressbook/rest/pass/login/JH_user_login", 
			method="POST")
	public String login(Request request, Response response)
	{
		try
		{
			UserInfo info = new Gson().fromJson(
					request.getBody().toString(Charsets.UTF_8), UserInfo.class );
			
			Optional<IAccount> acc = acc_service.getByPhoneId(info.username);
			if (!acc.isPresent())
			{
				return ErrorRet.ERROR(51006);
			}
			
			if (!acc.get().passwd().equals(info.password))
			{
				return ErrorRet.ERROR(51003);
			}
			
			IToken token = token_service.newToken(acc.get().id());
			
			return new Token_Data.Ret(token.id(), token.token()).toJson();
			
		}catch(JsonSyntaxException e)
		{
			throw new BadRequestException(e);
		}catch(Throwable e)
		{
			throw new ServiceException(e);
		}
	}
	
	@RequestMethod(value="/addressbook/rest/pass/login/JH_user_token_login/{token}", 
			method="GET")	
	public String loginWithToken(Request request, Response response)
	{
		String token = request.getHeader("token", "not param token ?");
		if (!token_service.getToken(token).isPresent())
		{
			return ErrorRet.ERROR(51008);
		}
		return ErrorRet.SUCESS();
	}
	
	
	static class PasswdAuthCode
	{
		String phoneNumber;
		String password;
		String authCode;
	}
	
	@RequestMethod(value="/addressbook/rest/pass/login/JH_user_password_update", 
			method="POST")		
	public String updatePasswdWithEms(Request request, Response response)
	{
		
		try
		{
			PasswdAuthCode info = new Gson().fromJson(
					request.getBody().toString(Charsets.UTF_8), PasswdAuthCode.class );
			
			Optional<IAccount> acc = acc_service.getByPhoneId(info.phoneNumber);
			if (!acc.isPresent())
			{
				return ErrorRet.ERROR(51006);
			}
			
			if (!ems_service.checkEmsVaild(info.phoneNumber, info.authCode, EMS_TYPE.GET_PWD))
			{
				return ErrorRet.ERROR(51011);
			}
			
			acc_service.modifyPasswd(acc.get().id(), info.password);
			return ErrorRet.SUCESS();
			
		}catch(JsonSyntaxException e)
		{
			throw new BadRequestException(e);
		}catch(Throwable e)
		{
			throw new ServiceException(e);
		}
	}
	
	static class TokenJson
	{
		String token;
	}
	
	@RequestMethod(value="/addressbook/rest/pass/login/JH_user_token_logout", 
			method="POST")		
	public String logout(Request request, Response response)
	{
		try
		{
			TokenJson info = new Gson().fromJson(
					request.getBody().toString(Charsets.UTF_8), TokenJson.class );
			
			Optional<IToken> token = token_service.getToken(info.token);
			
			if (!token.isPresent())
			{
				return ErrorRet.ERROR(20007);
			}
			
			token_service.expireToken(token.get());
			return ErrorRet.ERROR(20008);
			
		}catch(JsonSyntaxException e)
		{
			throw new BadRequestException(e);
		}catch(Throwable e)
		{
			throw new ServiceException(e);
		}
	}
	
	static class UserDetailInfo
	{
		String firstName;
		String lastName;
		String gender;
		String headUrl;
	}
	
	@RequestMethod(value="/addressbook/rest/auth/user/JH_user_update", 
			method="POST")		
	public String updateUserinfo(Request request, Response response)
	{
		
		String token = request.getHeader("token", "auth without Token?");
		
		try
		{
			final UserDetailInfo info = new Gson().fromJson(
					request.getBody().toString(Charsets.UTF_8), UserDetailInfo.class );
			
			Optional<IToken> itoken = token_service.getToken(token);
			
			if (!itoken.isPresent())
			{
				return ErrorRet.ERROR(20002);
			}
			
			Optional<IAccount> acc = acc_service.get(itoken.get().id());
			
			if (!acc.isPresent())
			{
				return ErrorRet.ERROR(20002);
			}
			
			new DefaultUserData(acc.get()) {
				
				@Override
				public Builder transform(Builder builder) {
		//			LOG.error("info.gender={}", info.gender);
					return builder.setFirstName(info.firstName)
							.setLastName(info.lastName)
							.setGender(Gender.valueOf(Integer.parseInt(info.gender)));
				}
			}.save();
			
			
			return ErrorRet.ERROR(20001);
			
		}catch(JsonSyntaxException e)
		{
			throw new BadRequestException(e);
		}catch(Throwable e)
		{
			throw new ServiceException(e);
		}
	}
	
	
	static class UsetInfoRet
	{
		String errorCode;
		UserDetailInfo data;
	}
	
	
	@RequestMethod(value="/addressbook/rest/auth/user/JH_user_info", 
			method="GET")		
	public String getUserinfo(Request request, Response response)
	{
		String token = request.getHeader("token", "auth without Token?");
		
		try
		{
			
			Optional<IToken> itoken = token_service.getToken(token);
			
			if (!itoken.isPresent())
			{
				return ErrorRet.ERROR(20002);
			}
			
			Optional<IAccount> acc = acc_service.get(itoken.get().id());
			
			if (!acc.isPresent())
			{
				return ErrorRet.ERROR(20002);
			}
			
			DefaultUserData userdata = new DefaultUserData(acc.get()) {
				
				@Override
				public Builder transform(Builder builder) {
					return null;
				}
			};
			
			userdata.load();
			
			UserDetailInfo info = new UserDetailInfo();
			if (userdata.data().hasFirstName())
			{
				info.firstName = userdata.data().getFirstName();
			}
			if (userdata.data().hasLastName())
			{
				info.lastName = userdata.data().getLastName();
			}
			if (userdata.data().hasGender())
			{
				info.gender = String.valueOf(
						userdata.data().getGender().getNumber());
			}
			if (userdata.data().hasHeadUrl())
			{
				info.headUrl = userdata.data().getHeadUrl();
			}
			
			UsetInfoRet inforet = new UsetInfoRet();
			inforet.data = info;
			inforet.errorCode = "20001";
			
			return new GsonBuilder().serializeNulls().create().toJson(inforet, UsetInfoRet.class);
			
		}catch(JsonSyntaxException e)
		{
			throw new BadRequestException(e);
		}catch(Throwable e)
		{
			throw new ServiceException(e);
		}
	}
	
	static class ErrorCodeImgRet
	{
		public ErrorCodeImgRet(String url) {
			data = new _data();
			data.headUrl = url;
		}
		
		String errorCode = "0";
		_data data;
		static class _data
		{
			String headUrl;
		}
	}
	
	@RequestMethod(value="/addressbook/rest/auth/user/JH_user_save_img", 
			method="POST")	
	public String uploadUserFaceImg(Request request, Response response)
	{
		
		String token = request.getHeader("token", "auth without Token?");
		
		try
		{
			
			Optional<IToken> itoken = token_service.getToken(token);
			
			if (!itoken.isPresent())
			{
				return ErrorRet.ERROR(20002);
			}
			
			Optional<IAccount> acc = acc_service.get(itoken.get().id());
			
			if (!acc.isPresent())
			{
				return ErrorRet.ERROR(20002);
			}
			
			IPhotoService service = new DefaultPhoto.Service();
			IPhoto photo = new DefaultPhoto(request.getBody().array());
			final IUrl url = service.upload(photo);
			
			DefaultUserData userdata = new DefaultUserData(acc.get()) {
				
				@Override
				public Builder transform(Builder builder) {
					return builder.mergeFrom(data).setHeadUrl(url.url());
				}
			};
			
			userdata.load();
			userdata.save();//save to the db
			
			return new Gson().toJson(new ErrorCodeImgRet(url.url()), ErrorCodeImgRet.class);
			
		}catch(AccountException e)
		{
			return ErrorRet.ERROR(20002);
		}catch(Throwable e)
		{
			throw new ServiceException(e);
		}
		
	}
	
	static class UserUpdatePwd
	{
		String oldPassword;
		String password;
		String passwordConfirm;
	}
	
	@RequestMethod(value="/addressbook/rest/auth/user/JH_user_password", 
			method="POST")	
	public String updatePasswd(Request request, Response response)
	{
		String token = request.getHeader("token", "auth without Token?");
		
		try
		{
			
			Optional<IToken> itoken = token_service.getToken(token);
			
			if (!itoken.isPresent())
			{
				return ErrorRet.ERROR(20002);
			}
			
			Optional<IAccount> acc = acc_service.get(itoken.get().id());
			
			if (!acc.isPresent())
			{
				return ErrorRet.ERROR(20002);
			}
			
			final UserUpdatePwd info = new Gson().fromJson(
					request.getBody().toString(Charsets.UTF_8), UserUpdatePwd.class );
			
			if (info.oldPassword.equals(acc.get().passwd())
					&& info.password.equals(info.passwordConfirm))
			{
				acc_service.modifyPasswd(acc.get().id(), info.password);
				return ErrorRet.SUCESS();
			}
			else
			{
				return ErrorRet.ERROR(20002);
			}
			
		}catch(JsonSyntaxException e)
		{
			throw new BadRequestException(e);
		}catch(Throwable e)
		{
			throw new ServiceException(e);
		}
	}
	
	
}
