package com.mm.account.ems;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

abstract public class CacheEmsService implements IEmsFactory
{
	
	static int DURATION_EMS = 60;
	LoadingCache<String, IEms> _emsCache = 
			CacheBuilder.newBuilder()
			.expireAfterWrite(DURATION_EMS, TimeUnit.SECONDS)
			.build(new CacheLoader<String, IEms>()
					{
						@Override
						public IEms load(String key) throws Exception {
							IEms ems = newEms(key);
							send(ems);
							return ems;
						}
					}
			);
	
	
	public LoadingCache<String, IEms> getEmsCache()
	{
		return _emsCache;
	}
	
	
}
