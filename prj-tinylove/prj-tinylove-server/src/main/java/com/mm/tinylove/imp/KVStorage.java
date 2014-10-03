package com.mm.tinylove.imp;


abstract public class KVStorage implements IKVStorage {

	public static long INVAID_KEY = -1;
	
	protected long id;

	public KVStorage(long id) {
		this.id = id;
	}

	String uniqKey() {
		return getClass().getCanonicalName();
	}

	public long id() {
		if (id == INVAID_KEY) {
			id = Ins.getUniqService()
					.nextID(uniqKey());
		}
		return id;
	}

	public String getKey() {
		return uniqKey() + ":" + id();
	}

	@Override
	public String marshalKey() {
		return getKey();
	}

	@Override
	@Deprecated
	public void unmarshalKey(String k) {

	}

}
