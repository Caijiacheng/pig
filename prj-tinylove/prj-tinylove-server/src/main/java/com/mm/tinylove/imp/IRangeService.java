package com.mm.tinylove.imp;

import java.util.List;

/**
 * 
 * Q: !完全不支持remove操作!.
 * A: 这还用废话吗?
 * 
 * 
 * @author apple
 *
 */

public interface IRangeService<E> {

	List<E> loadRange(String key, long begin, long end);
	Long lsize(String key);
	Long lpush(String key, List<E> data);
}
