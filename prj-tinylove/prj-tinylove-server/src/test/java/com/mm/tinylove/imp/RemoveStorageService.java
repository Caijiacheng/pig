package com.mm.tinylove.imp;

import java.nio.charset.StandardCharsets;

import redis.clients.jedis.Jedis;

public class RemoveStorageService extends DefaultStorageService {
	
	@Override
	public void remove(String key) {
		try (Jedis con = dbhandle.getConn()) {
			con.del(key.getBytes(StandardCharsets.UTF_8));
		}
	}
	
	@Override
	public void cleanStorage() {
		try (Jedis con = dbhandle.getConn()) {
			con.flushDB();
		}
	}
}
