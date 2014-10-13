package com.mm.tinylove.imp;

import java.util.List;


public interface IStorageService {
	
	<T extends IStorage> T load(T ins) ;
	<T extends IStorage> void save(T ins);
	
	<T extends IStorage> void saveCollection(List<IStorage> inslist);
	
	void checkAndSaveInTransaction(List<Object> inslist);
	
	long time();
	
	void remove(String key);
	
	void cleanStorage();
	
}
