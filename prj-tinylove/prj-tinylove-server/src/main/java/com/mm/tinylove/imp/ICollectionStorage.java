package com.mm.tinylove.imp;

import java.util.Collection;

@Deprecated
public interface ICollectionStorage {
	Collection<IStorage> saveCollections();
	void add2Save(IStorage storage);
}
