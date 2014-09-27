package com.mm.tinylove.imp;

import com.google.common.base.Charsets;

abstract public class KVStorage implements IStorage {

	protected long id;

	public KVStorage(long id) {
		this.id = id;
	}

	public KVStorage() {
		this.id = -1;

	}

	String uniqKey() {
		return getClass().getCanonicalName();
	}

	public long id() {
		if (id == -1) {
			id = Ins.getUniqService()
					.nextID(uniqKey().getBytes(Charsets.UTF_8));
		}
		return id;
	}

	public String getKey() {
		return uniqKey() + ":" + id();
	}

	@Override
	public byte[] marshalKey() {
		return getKey().getBytes(Charsets.UTF_8);
	}

	@Override
	@Deprecated
	public void unmarshalKey(byte[] data) {

	}

}
