package com.mm.tinylove;

import java.util.List;
import java.util.Set;

public interface IRandSet<E> {
	List<E> randMember(int count);
	List<E> all();
	
	long size();
	
	@Deprecated
	Set<E> srandMember(int count);
	@Deprecated
	Set<E> sall();
	
	void remove(E e);

	void sadd(E e);
	
	Set<E> saddCollection();
	
	
	void cleanAdd();
}
