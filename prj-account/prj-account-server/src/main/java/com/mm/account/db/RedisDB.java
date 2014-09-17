package com.mm.account.db;

import java.util.List;
import java.util.Properties;

import com.google.common.base.Splitter;
import com.mm.account.server.Config;
import com.mm.util.db.RedisHandle;

public class RedisDB extends RedisHandle {
	
	static {
		Properties prop = Config.ins().getProperties();
		List<String> list = Splitter.on(":").splitToList(
				prop.getProperty(Config.PROP_REDIS_SERVER));

		RedisHandle.getDefaultConfig().host = list.get(0);
		if (list.size() > 1) {
			RedisHandle.getDefaultConfig().port = 
					Integer.parseInt(list.get(1));
		}

		if (prop.containsKey(Config.PROP_REDIS_PASSWORD)) {
			RedisHandle.getDefaultConfig().passwd = 
					prop.getProperty(Config.PROP_REDIS_PASSWORD);
		}

		if (prop.containsKey(Config.PROP_REDIS_DBNUM)) {
			RedisHandle.getDefaultConfig().dbnum = 
					Integer.parseInt(prop.getProperty(Config.PROP_REDIS_DBNUM));
		}

	}

	
}
