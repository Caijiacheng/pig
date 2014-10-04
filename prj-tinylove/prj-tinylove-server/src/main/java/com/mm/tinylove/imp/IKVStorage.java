package com.mm.tinylove.imp;

public interface IKVStorage extends IStorage  {

	
	@Deprecated
	void unmarshalKey(String data);
	
	byte[] marshalValue();
	void unmarshalValue(byte[] data);
	
}
