package com.mm.tinylove.imp;

import java.util.List;
import java.util.Set;

/**
 * 
 * 
 * @author apple
 *
 */

public interface ICollectionService<E> {

	List<E> lloadRange(String key, long begin, long end);
	Long lsize(String key);
	Long lpush(String key, List<E> data);
	void lrem(String key, E e);
	//
	
	Set<E> smem(String key, int count);
	
	void sadd(String key, Set<E> data);
	Set<E> sall(String key);
	void srem(String key, E mem);
	
}
