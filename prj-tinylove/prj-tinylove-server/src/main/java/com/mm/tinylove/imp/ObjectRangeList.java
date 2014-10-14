package com.mm.tinylove.imp;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mm.tinylove.IObject;
import com.mm.tinylove.IRangeList;

/**
 * 这里只做ID的集合存储.对push的对象的save()操作不做处理
 * 
 * @author apple
 * 
 * @param <E>
 */
public abstract class ObjectRangeList<E extends IObject> implements
		IRangeList<E>, Function<Long, E> {

	IRangeList<Long> idrange;

	public ObjectRangeList(String key) {
		idrange = LongRangeList.getIns(key);
	}

	public ObjectRangeList(IRangeList<Long> idrange) {
		this.idrange = idrange;
	}

	@Override
	public List<E> range(long begin, long end) {
		return Lists.transform(idrange.range(begin, end), this);
	}

	@Override
	public long size() {
		return idrange.size();
	}

	// NOTE: this is lazy load
	@Override
	public List<E> all() {
		return Lists.transform(idrange.all(), this);
	}

	@Override
	public void lpush(E e) {
		idrange.lpush(e.id());
	}

	@Override
	public List<E> savelpushCollection() {
		throw new UnsupportedOperationException();
		// return Lists.transform(idrange.savelpushCollection(), this);
	}
	
	@Override
	public boolean exist(E ins)
	{
		return idrange.exist(ins.id());
	}

}
