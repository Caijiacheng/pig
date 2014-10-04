package com.mm.tinylove.imp;

import java.util.List;


public interface IStorageService {
	
	<T extends IStorage> T load(T ins) ;
	<T extends IStorage> void save(T ins);
	
	
	<T extends IStorage> void saveInTransaction(List<IStorage> inslist);
	

	
	void remove(String key);
	
	void cleanStorage();
	
}
