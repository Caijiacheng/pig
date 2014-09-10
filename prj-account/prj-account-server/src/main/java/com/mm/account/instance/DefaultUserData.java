package com.mm.account.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mm.account.db.RedisDB;
import com.mm.account.error.DBException;
import com.mm.account.proto.Account.UserData;

/**
 * 这里的class封装肯定有问题.但暂时先这样.以后再找更合适的方式来处理
 * 
 * @author caijiacheng
 *
 */


abstract public class DefaultUserData implements IVersion, ILoad, ISave {

	protected UserData data;
	protected IAccount acc;
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
	
	final static Logger LOG = LoggerFactory.getLogger(DefaultUserData.class);
	
	abstract public UserData.Builder transform(UserData.Builder builder);
	
	public UserData data()
	{
		return data;
	}
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
				if (acc.version() != 0)
				{
					LOG.error("DB didnot store the UserData correct? {}", acc );
				}
				data = UserData.newBuilder().setUid(acc.id()).setVersion(acc.version()).build();
//				LOG.error("init userdata:{}", data.toString());
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
	
	/**
	 * XXX:这里increment是使用mysql中的version,存储具体信息是使用redis,
	 * 这里在version变化的时候,没有保证一致性.有断电的时候,这里可能会造成错误
	 * 
	 * FIXME: increment的时候,使用mysql的行数据的事务锁来解决这里的问题
	 */
	
	@Override
	public void save() {
		
		increment();
		RedisDB db = new RedisDB();
		try(Jedis handle = db.getConn())
		{
			UserData.Builder ud = UserData.newBuilder();
			data = transform(ud).setUid(acc.id()).setVersion(acc.version()).build();
			Preconditions.checkNotNull(data);
			Preconditions.checkArgument(data.getVersion() == acc.version(), "need to set the same version");
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
