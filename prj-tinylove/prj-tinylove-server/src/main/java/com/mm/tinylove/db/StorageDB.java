package com.mm.tinylove.db;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Splitter;
import com.mm.util.db.RedisHandle;

public class StorageDB extends RedisHandle {

	static Conf storage_conf;

	public StorageDB() {

		super(storage_conf);
	}

	static {

		final String DEFAULT_PROPERTIES = "storage.propertis";

		Properties prop = new Properties();
		try {
			prop.load(ClassLoader.getSystemResourceAsStream(DEFAULT_PROPERTIES));
		} catch (IOException e) {
			throw new RuntimeException("Init Conf failed", e);
		}

		if (prop.containsKey("storage.redis.server")) {
			List<String> list = Splitter.on(":").splitToList(
					prop.getProperty("storage.redis.server"));

			prop.setProperty("storage.redis.server.host", list.get(0));

			System.err.println("host:"
					+ prop.getProperty("storage.redis.server.host"));

			if (list.size() > 1) {
				prop.setProperty("storage.redis.server.port", list.get(1));
			}
		}

		storage_conf = new Conf();
		storage_conf.host = prop.getProperty("storage.redis.server.host");
		storage_conf.passwd = prop.getProperty("storage.redis.password", null);
		storage_conf.port = Integer.parseInt(prop.getProperty(
				"storage.redis.server.port", "6397"));
		storage_conf.dbnum = Integer.parseInt(prop.getProperty(
				"storage.redis.dbnum", "0"));

	}

}
