package com.mm.tinylove.imp;

import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.mm.tinylove.IObject;
import com.mm.tinylove.IRandSet;

abstract public class ObjectRandSet<E extends IObject> implements IRandSet<E>, Function<Long, E>{

	IRandSet<Long> idset;
	
	public ObjectRandSet(String key)
	{
		idset = new LongRandSet(key);
	}
	
	public ObjectRandSet(IRandSet<Long> idset)
	{
		this.idset = idset;
	}
	
	@Override
	@Deprecated
	public Set<E> srandMember(int count) {
		return null;
	}

	@Override
	public long size() {
		return idset.size();
	}

	@Override
	@Deprecated
	public Set<E> sall() {
		return null;
	}

	@Override
	public void remove(E e) {
		idset.remove(e.id());
	}

	
	public class DelayLoadSet extends TreeSet<E>
	{
		
	}
	
	@Override
	public void sadd(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<E> saddCollection() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void cleanAdd() {
		throw new UnsupportedOperationException();
	}

}
