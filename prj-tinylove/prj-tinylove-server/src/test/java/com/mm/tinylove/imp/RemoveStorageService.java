package com.mm.tinylove.imp;

import redis.clients.jedis.Jedis;

public class RemoveStorageService extends DefaultStorageService {
	
	@Override
	public void remove(String key) {
		try (Jedis con = dbhandle.getConn()) {
			con.del(key);
		}
	}
}
