package com.mm.tinylove.imp;

public interface IKVStorage extends IStorage  {

	
	@Deprecated
	void unmarshalKey(String data);
	
	String marshalValue();
	void unmarshalValue(String data);
	
}
