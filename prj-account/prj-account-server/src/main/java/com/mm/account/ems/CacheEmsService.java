package com.mm.account.ems;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 这里的EMS只是简单做了个Cache,比较合理的方式应该是放在共享内存中.
 * 这个部分只是简单实现,这里违反了Restful的无状态性的规则
 * @author apple
 *
 */

abstract public class CacheEmsService implements IEmsFactory, IEmsService
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
}
