package com.mm.util.db;

public interface IDBHandle<T> {

	T getConn();
	
	T getSharedConn();
	
	void close(T h);
}
