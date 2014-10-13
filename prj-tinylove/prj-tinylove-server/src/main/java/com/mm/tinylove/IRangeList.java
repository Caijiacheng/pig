package com.mm.tinylove;

import java.util.List;

public interface IRangeList<E> {
	List<E> range(long begin, long end);
	
	long size();
	
	void lpush(E e);
	
	List<E> savelpushCollection();
	
	List<E> all();
	
	boolean exist(E ins);

	
}
