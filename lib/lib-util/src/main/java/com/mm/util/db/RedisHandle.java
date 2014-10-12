package com.mm.util.db;

import redis.clients.jedis.Jedis;

import com.google.common.base.Preconditions;

public class RedisHandle extends AbsDBHandle<Jedis> {
	static public class Conf
	{
		public String host;
		public int port = 6379;
		public int dbnum = 0;
		public String passwd = null;
		
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
	
	private Conf conf;
	
	public RedisHandle(Conf conf)
	{
		this.conf = conf;
		Preconditions.checkNotNull(this.conf);
		Preconditions.checkNotNull(this.conf.host);
	}
	
	public RedisHandle() {
		this(default_config);
	}
	
	@Override
	protected
	Jedis doConnect() {
		Jedis handle = null;
		if (!s_conn_share_mode.get())
		{
			handle = new Jedis(this.conf.host, this.conf.port);
		}else
		{
			handle = new WrapperJedis(this.conf.host, this.conf.port);
		}
		
		if (this.conf.passwd != null)
			handle.auth(this.conf.passwd);
		handle.select(this.conf.dbnum);
		return handle;
	}

	
	@Override
	public void close(Jedis h) {
		h.close();
	}
	
	
	
}
