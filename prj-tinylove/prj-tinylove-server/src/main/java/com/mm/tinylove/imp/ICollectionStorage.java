package com.mm.tinylove.imp;

import java.util.Collection;

public interface ICollectionStorage {
	Collection<IStorage> saveCollections();
	void add2Save(IStorage storage);
}
