package com.mm.auth.token;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;


public class DefaultToken extends PojoToken {
	
//	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
//	public static String bytesToHex(byte[] bytes) {
//	    char[] hexChars = new char[bytes.length * 2];
//	    for ( int j = 0; j < bytes.length; j++ ) {
//	        int v = bytes[j] & 0xFF;
//	        hexChars[j * 2] = hexArray[v >>> 4];
//	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//	    }
//	    return new String(hexChars);
//	}
	
	String getTokenKey()
	{
		return "TOKEN:" + _token;
	}
	
	public static class Service implements ITokenService
	{

		static int TOKEN_VAILD_PERIOD = 7 * 24 * 60 * 60;
		
		String getTokenIDKey(String userid)
		{
			return "TOKEN:" + "USERID:" + userid; 
		}
		
		@Override
		public IToken newToken(String id) {
			
			String st = id + "_" + Long.toString(System.currentTimeMillis());
			
			
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("md5");
				md.update(st.getBytes());
				
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}

			String md5 = BaseEncoding.base32Hex().encode(md.digest());
			DefaultToken token = new DefaultToken();
			
			token._id = id;
			token._token = md5;
			token._duration = TOKEN_VAILD_PERIOD;
			
			TokenDB db = new TokenDB();
			try(Jedis jh = db.getConn())
			{
				Pipeline pipe =  jh.pipelined();
				pipe.set(token.getTokenKey(), token.id());
				pipe.expire(token.getTokenKey(), token.duration());
				pipe.set(getTokenIDKey(token.id()), token.token());
				pipe.expire(getTokenIDKey(token.id()), token.duration());
				pipe.sync();
			}
			
			return token;
		}

		@Override
		public boolean checkValid(IToken token) {
			DefaultToken t = (DefaultToken)token;
			TokenDB db = new TokenDB();
			try(Jedis jh = db.getConn())
			{
				Pipeline pipe = jh.pipelined();
				Response<String> t1 = pipe.get(t.getTokenKey());
				Response<String> t2 = pipe.get(getTokenIDKey(t.id()));
				pipe.sync();
				
				return Objects.equals(t1.get(), t.id()) && Objects.equals(t2.get(), t.token());
			}
		}

		static final Logger LOG = LoggerFactory.getLogger(Service.class);
		
		@Override
		public Optional<IToken> getToken(String token) {
			DefaultToken t = new DefaultToken();
			t._token = token;
			
			TokenDB db = new TokenDB();
			try(Jedis jh = db.getConn())
			{
				String id = jh.get(t.getTokenKey());
				if (id == null)
				{
					return Optional.absent();
				}
				//comfirm
				t._id = id;
				String tt = jh.get(getTokenIDKey(t.id()));
				if (!Objects.equals(tt, token))
				{
					//
					LOG.warn("Consist Error?token:{}, id:{}", token, id);
					jh.del(t.getTokenKey());
					jh.del(getTokenIDKey(t.id()));
					return Optional.absent();
				}
			}
			return Optional.of((IToken)t);
		}
		
		
		@Override
		public void expireToken(IToken token) {
			
			DefaultToken t = (DefaultToken)token;
			
			TokenDB db = new TokenDB();
			try(Jedis jh = db.getConn())
			{
				Pipeline pipe = jh.pipelined();
				pipe.del(t.getTokenKey());
				pipe.del(getTokenIDKey(t.id()));
				pipe.sync();
			}
		}

		public boolean ping() {
			TokenDB db = new TokenDB();
			
			try(Jedis jh = db.getConn())
			{
				jh.get("1");
				return true;
			}catch (Throwable e) {
				return false;
			}
		}


	}
	
}
