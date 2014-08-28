package com.mm.account.db;


abstract public class AbsDBHandle<T> implements IDBHandle<T> {
	ThreadLocal<T> s_conn_threadlocal = 
			new ThreadLocal<T>();
	abstract T doConnect();
	
	@Override
	public T getConn() {
		return doConnect();
	}
	
	@Override
	public T getSharedConn() {
		T h = s_conn_threadlocal.get();
		if (h == null)
		{
			h = doConnect();
		}
		return h;
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		T h = s_conn_threadlocal.get();
		if (h != null)
		{
			close(h);
		}
	}
	
}
