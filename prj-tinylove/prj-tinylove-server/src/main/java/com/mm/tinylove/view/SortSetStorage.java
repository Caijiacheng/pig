package com.mm.tinylove.view;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.google.common.base.Function;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.mm.tinylove.db.StorageDB;

/**
 * 这里的实现与 com.mm.tinylove.imp完全没有关系
 * @author caijiacheng
 *
 * @param <E>
 */


public abstract class SortSetStorage<E> implements ISortSetView<E> {

	StorageDB db = new StorageDB();

	String key;
	byte[] rawkey;
	double min;
	double max;
	boolean inbound = false;

	public SortSetStorage(String key) {
		this.key = key;
		this.rawkey = key.getBytes(StandardCharsets.UTF_8);
	}

	String getKey() {
		return key;
	}

	abstract byte[] marshalValue(E obj);

	abstract E unmarshalValue(byte[] bs);

	void setBound(double min, double max) {
		Verify.verify(max > min);
		this.min = min;
		this.max = max;
		this.inbound = true;
	}

	@Override
	public List<E> range(long begin, long end) {
		try (Jedis con = db.getConn()) {
			Set<byte[]> data;
			if (this.inbound) {
				Verify.verify(end > begin);
				data = con.zrangeByScore(rawkey, min, max, (int) begin,
						(int) (end - begin));
			} else {
				data = con.zrange(rawkey, begin, end);
			}
			return Lists.transform(Lists.newArrayList(data),
					new Function<byte[], E>() {
						public E apply(byte[] v) {
							return unmarshalValue(v);
						}
					});
		}

	}

	@Override
	public List<E> all() {
		try (Jedis con = db.getConn()) {
			return Lists.transform(
					Lists.newArrayList(con.zrange(rawkey, 0, -1)),
					new Function<byte[], E>() {
						public E apply(byte[] v) {
							return unmarshalValue(v);
						}
					});
		}
	}

	@Override
	public long size() {
		try (Jedis con = db.getConn()) {
			return con.zcard(rawkey);
		}
	}

	@Override
	public long rank(E obj) {
		try (Jedis con = db.getConn()) {
			return con.zrank(rawkey, marshalValue(obj));
		}
	}

	@Override
	public void add(E obj, double score) {
		if (this.inbound)
		{
			if (score < min || score > max)
			{
				//skip
				return;
			}
		}
		try (Jedis con = db.getConn()) {
			con.zadd(rawkey, score, marshalValue(obj));
		}
	}
}
