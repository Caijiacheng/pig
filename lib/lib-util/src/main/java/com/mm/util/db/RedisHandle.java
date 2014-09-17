package com.mm.util.db;

import redis.clients.jedis.Jedis;

import com.google.common.base.Preconditions;

public class RedisHandle extends AbsDBHandle<Jedis> {
	static public class Conf
	{
		public String host;
		public int port = 3306;
		public int dbnum = 0;
		public String passwd;
		
	}
	
	static private Conf default_config = null;
	
	static public Conf getDefaultConfig()
	{
		if (default_config == null)
		{
			default_config = new Conf();
		}
		return default_config;
	}
	
	
	public RedisHandle() {
		Preconditions.checkNotNull(default_config);
		Preconditions.checkNotNull(default_config.host);
	}
	
	@Override
	protected
	Jedis doConnect() {
		Jedis handle = null;
		if (!s_conn_share_mode.get())
		{
			handle = new Jedis(getDefaultConfig().host, getDefaultConfig().port);
		}else
		{
			handle = new WrapperJedis(getDefaultConfig().host, getDefaultConfig().port);
		}
		
		if (getDefaultConfig().passwd != null)
			handle.auth(getDefaultConfig().passwd);
		handle.select(getDefaultConfig().dbnum);
		return handle;
	}

	
	@Override
	public void close(Jedis h) {
		h.close();
	}
	
	
	
}
