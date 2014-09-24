package com.mm.auth.token;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Splitter;
import com.mm.util.db.RedisHandle;

public class TokenDB extends RedisHandle {
	
	static Conf token_conf;
	
	
	public TokenDB() {
		
		super(token_conf);
	}
	
	static {
		
		final String DEFAULT_PROPERTIES = "token.propertis";
		
		Properties prop = new Properties();
		try
		{
			prop.load(ClassLoader.getSystemResourceAsStream(DEFAULT_PROPERTIES));
		} catch (IOException e) {
			throw new RuntimeException("Init Conf failed", e);
		}
		
		if (prop.containsKey("token.redis.server"))
		{
			List<String> list = Splitter.on(":").splitToList(
					prop.getProperty("token.redis.server"));
			
			prop.setProperty("token.redis.server.host", list.get(0));
			
			System.err.println("host:" + prop.getProperty("token.redis.server.host"));
			
			if (list.size() > 1) {
				prop.setProperty("token.redis.server.port", list.get(1));
			}
		}

		token_conf = new Conf();
		token_conf.host = prop.getProperty("token.redis.server.host");
		token_conf.passwd = prop.getProperty("token.redis.password", null);
		token_conf.port = Integer.parseInt(prop.getProperty("token.redis.server.port", "6397"));
		token_conf.dbnum = Integer.parseInt(prop.getProperty("token.redis.dbnum", "0"));


	}

	
}
