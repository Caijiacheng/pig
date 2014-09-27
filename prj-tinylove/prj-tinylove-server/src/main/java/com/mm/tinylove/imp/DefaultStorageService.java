package com.mm.tinylove.imp;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.google.common.base.Charsets;
import com.mm.tinylove.db.StorageDB;

/*
 * TODO: 优化的方法:
 * 	1. laod的时候,可以增加一个cache,把一些不变的变量放到cache中去.这样来合理利用内存.比如说评论等内容.这样就可以整个服务器共享load出来的内容
 * 		cache: 内存cache or levelDB cache
 */

public class DefaultStorageService implements IStorageService, IUniqService
		{

	StorageDB dbhandle = new StorageDB();

	@Override
	public <T extends IStorage> T load(T ins){
		try (Jedis con = dbhandle.getConn()) {
			byte[] key = ins.marshalKey();
			byte[] value = con.get(key);
			ins.unmarshalValue(value);
		}
		return ins;
	}

	@Override
	public <T extends IStorage> void save(T ins) {
		try (Jedis con = dbhandle.getConn()) {
			byte[] key = ins.marshalKey();
			con.set(key, ins.marshalValue());
		}
	}

	byte[] MSG_KEY_INCR = (this.getClass().getCanonicalName() + ":MSG")
			.getBytes(Charsets.UTF_8);


	@Override
	public Long nextID(byte[] key) {
		try(Jedis con = dbhandle.getConn())
		{
			return con.incr(key);
		}
	}

	@Override
	public Long curID(byte[] key) {
		try(Jedis con = dbhandle.getConn())
		{
			return Long.parseLong(new String(con.get(key), Charsets.UTF_8));
		}
	}

	@Override
	public boolean exist(byte[] key) {
		try(Jedis con = dbhandle.getConn())
		{
			return con.exists(key);
		}
	}
	
	

	
	

}
