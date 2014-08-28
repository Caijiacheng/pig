package com.mm.account.db;

public interface IDBHandle<T> {

	T getConn();
	
	T getSharedConn();
	
	void close(T h);
}
