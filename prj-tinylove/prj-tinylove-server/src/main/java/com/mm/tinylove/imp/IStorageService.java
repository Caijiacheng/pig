package com.mm.tinylove.imp;


public interface IStorageService {
	
	<T extends IStorage> T load(T ins) ;
	<T extends IStorage> void save(T ins);
	
	boolean exist(byte[] key);
	
}
