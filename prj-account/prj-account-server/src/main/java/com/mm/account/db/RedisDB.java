package com.mm.account.db;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.mm.account.server.Config;

import redis.clients.jedis.Jedis;

public class RedisDB extends AbsDBHandle<Jedis> {
	
	@Override
	Jedis doConnect() {
		Properties prop = Config.ins().getProperties();
		try {
			Jedis handle = new Jedis(new URI(prop.getProperty(Config.PROP_REDIS_SERVER)));
			handle.auth(prop.getProperty(Config.PROP_REDIS_PASSWORD));
			handle.select(Integer.parseInt(prop.getProperty(Config.PROP_REDIS_DBNUM)));
			return handle;
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public
	void close(Jedis h) {
		h.close();
	}
}
