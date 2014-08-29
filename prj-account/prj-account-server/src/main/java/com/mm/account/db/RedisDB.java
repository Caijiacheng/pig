package com.mm.account.db;

import java.util.List;
import java.util.Properties;

import redis.clients.jedis.Jedis;

import com.google.common.base.Splitter;
import com.mm.account.server.Config;

public class RedisDB extends AbsDBHandle<Jedis> {

	static String host;
	static int port = 3306;
	static String passwd;
	static int dbnum = 0;

	static {
		Properties prop = Config.ins().getProperties();
		List<String> list = Splitter.on(":").splitToList(
				prop.getProperty(Config.PROP_REDIS_SERVER));

		host = list.get(0);
		if (list.size() > 1) {
			port = Integer.parseInt(list.get(1));
		}

		if (prop.containsKey(Config.PROP_REDIS_PASSWORD)) {
			passwd = prop.getProperty(Config.PROP_REDIS_PASSWORD);
		}

		if (prop.containsKey(Config.PROP_REDIS_DBNUM)) {
			dbnum = Integer.parseInt(prop.getProperty(Config.PROP_REDIS_DBNUM));
		}

	}

	@Override
	Jedis doConnect() {
		Jedis handle = new Jedis(host, port);
		if (passwd != null)
			handle.auth(passwd);
		handle.select(dbnum);
		return handle;
	}

	@Override
	public void close(Jedis h) {
		h.close();
	}
}
