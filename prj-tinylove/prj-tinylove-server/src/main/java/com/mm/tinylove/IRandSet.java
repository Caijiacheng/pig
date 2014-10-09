package com.mm.tinylove;

import java.util.List;
import java.util.Set;

public interface IRandSet<E> {
	Set<E> randMember(int count);
	
	long size();
	
	Set<E> all();
	void remove(E e);

	void sadd();
	
	void saddCollection();
}
