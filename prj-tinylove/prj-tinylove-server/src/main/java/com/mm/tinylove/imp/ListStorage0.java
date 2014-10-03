package com.mm.tinylove.imp;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mm.tinylove.db.StorageDB;

@Deprecated
public class ListStorage0 implements List<Long> {

	StorageDB dbhandle = new StorageDB();
	String key;

	public ListStorage0(String key) {
		this.key = key;
	}

	@Override
	public Long get(int index) {
		try (Jedis con = dbhandle.getConn()) {
			String ret = con.lindex(key, (long) index);
			if (ret == null) {
				return null;
			}
			return Long.parseLong(con.lindex(key, (long) index));
		}
	}

	@Override
	public int size() {
		try (Jedis con = dbhandle.getConn()) {
			return con.llen(key).intValue();
		}
	}

	@Override
	public Long set(int index, Long element) {
		try (Jedis con = dbhandle.getConn()) {
			String value = con.lindex(key, index);
			con.lset(key, index, String.valueOf(element));
			return Long.parseLong(value);
		}
	}

	@Override
	public boolean add(Long e) {
		try (Jedis con = dbhandle.getConn()) {
			return con.lpush(key, String.valueOf(e)) == 1;
		}
	}

	public void add(int index, Long element) {
		try (Jedis con = dbhandle.getConn()) {
			con.linsert(key, LIST_POSITION.AFTER, String.valueOf(get(index)),
					String.valueOf(element));
		}
	}

	@Override
	public Iterator<Long> iterator() {
		return new Itr();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		try (Jedis con = dbhandle.getConn()) {
			List<String> values = con.lrange(key, 0, -1);
			for (String value : values) {
				boolean match = false;
				for (Object o : c) {
					if (!(o instanceof Long)) {
						return false;
					}
					if (Objects.equals(o, Long.parseLong(value))) {
						match = true;
						break;
					}
				}
				if (!match) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Long> c) {
		try (Jedis con = dbhandle.getConn()) {
			return con.lpush(key, c.toArray(new String[c.size()])) == c.size();
		}
	}

	// TODO:batch
	@Override
	public boolean addAll(int index, Collection<? extends Long> c) {
		for (Long cc : c) {
			add(index, cc);
		}
		return true;
	}

	// TODO:batch
	@Override
	public boolean removeAll(Collection<?> c) {
		try (Jedis con = dbhandle.getConn()) {

			for (Object o : c) {
				if (!(o instanceof Long)) {
					return false;
				}
				if (con.lrem(key, 1, String.valueOf(o)) <= 0) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		try (Jedis con = dbhandle.getConn()) {
			con.del(key);
		}

	}

	@Override
	public int indexOf(Object o) {
		try (Jedis con = dbhandle.getConn()) {
			List<String> values = con.lrange(key, 0, -1);
			return values.indexOf(o);
		}
	}

	@Override
	public int lastIndexOf(Object o) {
		try (Jedis con = dbhandle.getConn()) {
			List<String> values = con.lrange(key, 0, -1);
			return values.lastIndexOf(o);
		}
	}

	@Override
	public ListIterator<Long> listIterator() {
		return new ListItr(0);
	}

	@Override
	public ListIterator<Long> listIterator(int index) {
		return new ListItr(index);
	}

	@Override
	public List<Long> subList(int fromIndex, int toIndex) {

		try (Jedis con = dbhandle.getConn()) {
			List<String> values = con.lrange(key, fromIndex, toIndex);

			return Lists.transform(values, new Function<String, Long>() {
				public Long apply(String s) {
					return Long.parseLong(s);
				}
			});
		}
	}

	@Override
	public boolean isEmpty() {
		try (Jedis con = dbhandle.getConn()) {
			return con.llen(key).intValue() == 0;
		}
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Long)) {
			return false;
		}
		try (Jedis con = dbhandle.getConn()) {
			List<String> values = con.lrange(key, 0, -1);
			for (String value : values) {
				if (Objects.equals(Long.parseLong(value), o)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object[] toArray() {
		try (Jedis con = dbhandle.getConn()) {
			List<String> values = con.lrange(key, 0, -1);
			Object[] objs = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				objs[i] = Long.parseLong(new String(values.get(i)));
			}
			return objs;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {

		Object[] objs = toArray();
		if (a.length < objs.length)
			return (T[]) Arrays.copyOf(objs, objs.length, a.getClass());
		System.arraycopy(objs, 0, a, 0, objs.length);
		if (a.length > objs.length)
			a[objs.length] = null;
		return a;

	}

	@Override
	public boolean remove(Object o) {
		if (!(o instanceof Long)) {
			return false;
		}

		try (Jedis con = dbhandle.getConn()) {
			return con.lrem(key, 1, String.valueOf(o)) > 0;
		}
	}

	@Override
	public Long remove(int index) {
		try (Jedis con = dbhandle.getConn()) {
			String value = con.lindex(key, index);
			con.lrem(key, 1, value);
			return Long.parseLong(value);
		}
	}

	private class Itr implements Iterator<Long> {
		int cursor = 0;

		int lastRet = -1;

		public boolean hasNext() {
			return cursor != size();
		}

		public Long next() {
			try {
				int i = cursor;
				Long next = get(i);
				lastRet = i;
				cursor = i + 1;
				if (next == null) {
					throw new NoSuchElementException();
				}
				return next;
			} catch (IndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}

		public void remove() {
			if (lastRet < 0)
				throw new IllegalStateException();

			ListStorage0.this.remove(lastRet);
			if (lastRet < cursor)
				cursor--;
			lastRet = -1;
		}

	}

	private class ListItr extends Itr implements ListIterator<Long> {
		ListItr(int index) {
			cursor = index;
		}

		public boolean hasPrevious() {
			return cursor != 0;
		}

		public Long previous() {
			try {
				int i = cursor - 1;
				Long previous = get(i);
				lastRet = cursor = i;
				return previous;
			} catch (IndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}

		public int nextIndex() {
			return cursor;
		}

		public int previousIndex() {
			return cursor - 1;
		}

		public void set(Long e) {
			if (lastRet < 0)
				throw new IllegalStateException();

			try {
				ListStorage0.this.set(lastRet, e);
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		public void add(Long e) {

			try {
				int i = cursor;
				ListStorage0.this.add(i, e);
				lastRet = -1;
				cursor = i + 1;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

	}

}
