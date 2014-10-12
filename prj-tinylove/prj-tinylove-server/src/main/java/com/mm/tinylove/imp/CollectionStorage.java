package com.mm.tinylove.imp;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

abstract public class CollectionStorage extends KVStorage implements
		ICollectionStorage {

	public CollectionStorage(long id) {
		super(id);
	}

	Set<IStorage> ins2save = Sets.newHashSet();

	@Override
	public void add2Save(IStorage ins) {
		ins2save.add(ins);
	}

	@Override
	public Collection<IStorage> saveCollections() {
		Set<IStorage> ins2ret = ins2save;
		ins2save = Sets.newHashSet();
		return ins2ret;
	}

}
