package com.mm.tinylove.imp;

public interface IStorage {

	byte[] marshalKey();
	@Deprecated
	void unmarshalKey(byte[] data);
	
	byte[] marshalValue();
	void unmarshalValue(byte[] data);
	
}
