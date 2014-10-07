package com.mm.tinylove.view;

import java.util.List;

public interface ISortSetView<E> {

	List<E> range(long begin, long end);
	
	List<E> all();
	
	long size();
	
	long rank(E obj);
	
	void add(E obj, double score);
}
