package com.mm.account.instance;

import redis.clients.jedis.Jedis;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mm.account.db.RedisDB;
import com.mm.account.error.DBException;
import com.mm.account.proto.Account.UserData;

abstract public class DefaultUserData implements IVersion, ILoad, ISave {

	UserData data;
	IAccount acc;
	IAccountService acc_service = new DefaultAccount.Service();
	
	int load_version = -1;
	
	public DefaultUserData(long userid) {
		acc = acc_service.get(userid).get();
	}
	
	public DefaultUserData(IAccount acc)
	{
		this.acc = acc;
	}
	
	static public String getRedisKey(IAccount acc)
	{
		return String.format("DefaultUserData:%s:version:%s", acc.id(), acc.version());
	}
	
	abstract public <T> void transform(T obj);
	
	@Override
	public void load() {
		
		if (!needToSync())
		{
			return;
		}
		
		RedisDB db = new RedisDB();
		
		try(Jedis handle = db.getConn())
		{
			String s = handle.get(getRedisKey(acc));
			if (s == null)
			{
				data = UserData.newBuilder().build();
			}else
			{
				try {
					data = UserData.newBuilder().mergeFrom(s.getBytes(Charsets.UTF_8)).build();
				} catch (InvalidProtocolBufferException e) {
					throw new DBException(e);
				}
			}
		}
		
		load_version = acc.version();
	}

	boolean needToSync()
	{
		if (load_version == acc.version())
		{
			return false;
		}
		return true;
	}
	
	@Override
	public void save() {
		
		if (!needToSync())
		{
			return;
		}
		
		increment();
		
		RedisDB db = new RedisDB();
		try(Jedis handle = db.getConn())
		{
			Preconditions.checkNotNull(data);
			handle.set(getRedisKey(acc), new String(data.toByteArray(), Charsets.UTF_8));
		}
		
		load_version = acc.version();
		
	}

	@Override
	public int current() {
		return load_version;
	}

	int increment() {
		acc = acc_service.incrVersion(acc);
		return acc.version();
	}

}
