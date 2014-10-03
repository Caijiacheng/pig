package com.mm.tinylove.imp;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.db.StorageDB;

/*
 * TODO: 优化的方法:
 * 	1. laod的时候,可以增加一个cache,把一些不变的变量放到cache中去.这样来合理利用内存.比如说评论等内容.这样就可以整个服务器共享load出来的内容
 * 		cache: 内存cache or levelDB cache
 */

public class DefaultStorageService implements IStorageService, IUniqService,
		IRangeService<Long> {

	StorageDB dbhandle = new StorageDB();

	@Override
	public <T extends IStorage> T load(T ins) {
		try (Jedis con = dbhandle.getConn()) {
			String key = ins.marshalKey();
			String value = con.get(key);

			if (ins instanceof IKVStorage) {
				IKVStorage kv_ins = (IKVStorage) ins;
				kv_ins.unmarshalValue(value);
			}

		}
		return ins;
	}

	@Override
	public <T extends IStorage> void save(T ins) {
		try (Jedis con = dbhandle.getConn()) {

			if (ins instanceof IKVStorage) {
				IKVStorage kv_ins = (IKVStorage) ins;
				con.set(kv_ins.marshalKey(), kv_ins.marshalValue());
			}

		}
	}

	byte[] MSG_KEY_INCR = (this.getClass().getCanonicalName() + ":MSG")
			.getBytes(Charsets.UTF_8);

	@Override
	public boolean exist(byte[] key) {
		try (Jedis con = dbhandle.getConn()) {
			return con.exists(key);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IStorage> void saveInTransaction(List<IStorage> inslist) {

		try (Jedis con = dbhandle.getConn()) {
			Transaction t = con.multi();
			for (IStorage ins : inslist) {
				if (ins instanceof IKVStorage)
				{
					IKVStorage kv_ins = (IKVStorage) ins;
					t.set(kv_ins.marshalKey(), kv_ins.marshalValue());
				}else if (ins instanceof IRangeList)
				{
					lpush(ins.marshalKey(), ((IRangeList<Long>) ins).lpushCollection());
				}
					
				
				
			}
			t.exec();
		}

	}

	
	@Override
	public Long nextID(String key) {
		try(Jedis con = dbhandle.getConn())
		{
			return con.incr(key);
		}
	}

	@Override
	public Long curID(String key) {
		try(Jedis con = dbhandle.getConn())
		{
			return Long.parseLong(con.get(key));
		}
	}

	@Override
	public List<Long> loadRange(String key, long begin, long end) {
		
		try(Jedis con = dbhandle.getConn())
		{
			return Lists.transform(con.lrange(key, begin, end), new Function<String, Long>() {
				public Long apply(String k)
				{
					return Long.parseLong(k);
				}
			});
		}
		
	}

	@Override
	public Long lsize(String key) {
		
		try(Jedis con = dbhandle.getConn())
		{
			return con.llen(key);
		}
	}

	@Override
	public Long lpush(String key, List<Long> data) {
		try(Jedis con = dbhandle.getConn())
		{
			long s =  con.lpush(key, data.toArray(new String[data.size()]));
			Verify.verify(s == data.size());
			return s;
		}
	}
}
