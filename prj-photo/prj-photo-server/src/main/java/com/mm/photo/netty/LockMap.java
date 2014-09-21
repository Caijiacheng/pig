package com.mm.photo.netty;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

abstract public class LockMap<T, K > {
	private WeakHashMap<T, WeakReference<K>> _lockmap = new WeakHashMap<T, WeakReference<K>>();

	public synchronized K getLock(T t) {
		WeakReference<K> ref = _lockmap.get(t);
		if (ref != null) {
			K lock = ref.get();
			if (lock != null) {
				return lock;
			}
		}
		K lock = newLock();
		_lockmap.put(t, new WeakReference<K>(lock));
		return lock;
	}
	
	
	abstract K newLock();

}