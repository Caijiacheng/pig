package com.moshi.sde.util;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockMap<T>
{
	private WeakHashMap<T, WeakReference<Lock>> _lockmap = 
			new WeakHashMap<T, WeakReference<Lock>>();

	public synchronized Lock getLock(T t)
	{
		WeakReference<Lock> ref = _lockmap.get(t);
		if (ref != null)
		{
			Lock lock = ref.get();
			if (lock != null)
			{
				return lock;
			}
		}
		Lock lock = new ReentrantLock();
		_lockmap.put(t, 
				new WeakReference<Lock>(lock));
		return lock;
	}
	
}
