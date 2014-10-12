package com.mm.tinylove.imp;

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mm.tinylove.IObject;
import com.mm.tinylove.IRandSet;

abstract public class ObjectRandSet<E extends IObject> implements
		IRandSet<E>, Function<Long, E> {

	IRandSet<Long> idset;
	ICollectionStorage holder;

	public ObjectRandSet(String key, ICollectionStorage changeHolder) {
		idset = new LongRandSet(key);
		holder = changeHolder;
	}

	public ObjectRandSet(IRandSet<Long> idset,
			ICollectionStorage changeHolder) {
		this.idset = idset;
		holder = changeHolder;
	}

	// suggest user randmember() and all() which use delay load
	@Override
	@Deprecated
	public Set<E> srandMember(int count) {
		Set<E> members = Sets.newHashSet();
		for (long id : idset.randMember(count)) {
			members.add(this.apply(id));
		}
		return members;

	}

	@Override
	public long size() {
		return idset.size();
	}

	@Override
	@Deprecated
	public Set<E> sall() {
		Set<E> members = Sets.newHashSet();
		for (long id : idset.all()) {
			members.add(this.apply(id));
		}
		return members;
	}

	@Override
	public void remove(E e) {
		idset.remove(e.id());
		holder.add2Save((IStorage)idset);
	}

	@Override
	public void sadd(E e) {
		idset.sadd(e.id());
		holder.add2Save((IStorage)idset);
	}

	@Override
	public Set<E> saddCollection() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<E> sremCollection()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<E> randMember(int count) {
		return Lists.transform(idset.randMember(count), this);
	}

	@Override
	public List<E> all() {
		return Lists.transform(idset.all(), this);
	}
	
	

}
