package com.mm.tinylove.imp;

public interface IUniqService {
	Long nextID(byte[] key);
	Long curID(byte[] key);
	
}
