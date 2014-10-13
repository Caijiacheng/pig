package com.mm.tinylove.imp;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mm.tinylove.IRandSet;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.db.StorageDB;

/*
 * TODO: 优化的方法:
 * 	1. laod的时候,可以增加一个cache,把一些不变的变量放到cache中去.这样来合理利用内存.比如说评论等内容.这样就可以整个服务器共享load出来的内容
 * 		cache: 内存cache or levelDB cache
 *  2. getBytes() 调用次数很频繁,可以优化掉
 */

public class DefaultStorageService implements IStorageService, IUniqService,
		ICollectionService<Long> {

	StorageDB dbhandle = new StorageDB();

	@Override
	public <T extends IStorage> T load(T ins) {
		try (Jedis con = dbhandle.getConn()) {
			byte[] key = ins.marshalKey();
			byte[] value = con.get(key);

			if (ins instanceof IKVStorage) {
				IKVStorage kv_ins = (IKVStorage) ins;
				kv_ins.unmarshalValue(value);
			}
		}
		return ins;
	}

	@SuppressWarnings("unchecked")
	static void saveInPipeBase(Collection<IStorage> all_ins, Transaction con) {
		Queue<IStorage> q_ins = Queues.newArrayDeque(all_ins);

		while (!q_ins.isEmpty()) {
			IStorage ins = q_ins.poll();
			if (ins instanceof ICollectionStorage) {
				ICollectionStorage cs = (ICollectionStorage) ins;
				q_ins.addAll(cs.saveCollections());
			}

			if (ins instanceof IKVStorage) {
				IKVStorage kv_ins = (IKVStorage) ins;
				con.set(kv_ins.marshalKey(), kv_ins.marshalValue());
			} else if (ins instanceof IRangeList) {
				List<Long> data = ((IRangeList<Long>) ins)
						.savelpushCollection();

				if (data.size() != 0) {
					byte[][] bdata = new byte[data.size()][];

					for (int i = 0; i < data.size(); i++) {
						bdata[i] = String.valueOf(data.get(i)).getBytes(
								StandardCharsets.UTF_8);
					}
					con.lpush(ins.marshalKey(), bdata);
				}

			} else if (ins instanceof IRandSet) {
				Set<Long> data = ((IRandSet<Long>) ins).saddCollection();

				if (data.size() != 0) {
					byte[][] bdata = new byte[data.size()][];
					Object[] arr = data.toArray();
					for (int i = 0; i < data.size(); i++) {
						bdata[i] = String.valueOf((Long) (arr[i])).getBytes(
								StandardCharsets.UTF_8);
					}
					con.sadd(ins.marshalKey(), bdata);
				}

				Set<Long> rdata = ((IRandSet<Long>) ins).sremCollection();

				if (rdata.size() != 0) {
					byte[][] bdata = new byte[rdata.size()][];
					Object[] arr = rdata.toArray();
					for (int i = 0; i < rdata.size(); i++) {
						bdata[i] = String.valueOf((Long) (arr[i])).getBytes(
								StandardCharsets.UTF_8);
					}
					con.srem(ins.marshalKey(), bdata);
				}

			}
		}
	}

	@Override
	public <T extends IStorage> void save(T ins) {
		try (Jedis con = dbhandle.getConn()) {
			Transaction t = con.multi();
			saveInPipeBase(Lists.newArrayList((IStorage) ins), t);
			t.exec();
		}
	}

	byte[] MSG_KEY_INCR = (this.getClass().getCanonicalName() + ":MSG")
			.getBytes(Charsets.UTF_8);

	@Override
	public <T extends IStorage> void saveCollection(List<IStorage> inslist) {
		try (Jedis con = dbhandle.getConn()) {
			Transaction t = con.multi();
			saveInPipeBase(inslist, t);
			t.exec();
		}
	}

	@Override
	public void checkAndSaveInTransaction(List<Object> inslist) {
		saveCollection(Lists.transform(inslist,
				new Function<Object, IStorage>() {
					public IStorage apply(Object obj) {
						return (IStorage) obj;
					}
				}));
	}

	@Override
	public Long nextID(String key) {
		try (Jedis con = dbhandle.getConn()) {
			return con.incr(key);
		}
	}

	@Override
	public Long curID(String key) {
		try (Jedis con = dbhandle.getConn()) {
			return Long.parseLong(con.get(key));
		}
	}

	@Override
	public List<Long> lloadRange(String key, long begin, long end) {

		try (Jedis con = dbhandle.getConn()) {
			return Lists.transform(con.lrange(
					key.getBytes(StandardCharsets.UTF_8), begin, end),
					new Function<byte[], Long>() {
						public Long apply(byte[] k) {
							return Long.parseLong(new String(k,
									StandardCharsets.UTF_8));
						}
					});
		}

	}

	@Override
	public Long lsize(String key) {

		try (Jedis con = dbhandle.getConn()) {
			return con.llen(key.getBytes(StandardCharsets.UTF_8));
		}
	}

	@Override
	public Long lpush(String key, List<Long> data) {

		byte[] k = key.getBytes(StandardCharsets.UTF_8);
		try (Jedis con = dbhandle.getConn()) {
			Pipeline pp = con.pipelined();
			for (int i = 0; i < data.size(); i++) {
				pp.lpush(
						k,
						String.valueOf(data.get(i)).getBytes(
								StandardCharsets.UTF_8));
			}
			pp.sync();
			return (long) data.size();
		}
	}

	@Override
	public void remove(String key) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void cleanStorage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long time() {
		try (Jedis con = dbhandle.getConn()) {
			List<String> tt = con.time();
			return Long.parseLong(tt.get(0)) * 1000 + Long.parseLong(tt.get(1))
					/ 1000;
		}
	}

	@Override
	public void lrem(String key, Long e) {
		try (Jedis con = dbhandle.getConn()) {
			con.lrem(key.getBytes(StandardCharsets.UTF_8), 1, String.valueOf(e)
					.getBytes(StandardCharsets.UTF_8));
		}
	}

	@Override
	public Set<Long> srandmem(String key, int count) {
		try (Jedis con = dbhandle.getConn()) {
			return Sets.newTreeSet(Iterables.transform(con.srandmember(
					key.getBytes(StandardCharsets.UTF_8), count),
					new Function<byte[], Long>() {
						public Long apply(byte[] k) {
							return Long.parseLong(new String(k,
									StandardCharsets.UTF_8));
						}
					}));
		}
	}

	@Override
	public void sadd(String key, Set<Long> data) {
		try (Jedis con = dbhandle.getConn()) {
			byte[] k = key.getBytes(StandardCharsets.UTF_8);
			Pipeline pp = con.pipelined();
			for (Long d : data) {
				pp.sadd(k, String.valueOf(d).getBytes(StandardCharsets.UTF_8));
			}
			pp.sync();
			return;
		}
	}

	@Override
	public Set<Long> sall(String key) {
		try (Jedis con = dbhandle.getConn()) {
			return Sets.newTreeSet(Iterables.transform(
					con.smembers(key.getBytes(StandardCharsets.UTF_8)),
					new Function<byte[], Long>() {
						public Long apply(byte[] k) {
							return Long.parseLong(new String(k,
									StandardCharsets.UTF_8));
						}
					}));
		}
	}

	@Override
	public void srem(String key, Long mem) {
		try (Jedis con = dbhandle.getConn()) {
			con.srem(key.getBytes(StandardCharsets.UTF_8), String.valueOf(mem)
					.getBytes(StandardCharsets.UTF_8));
		}
	}

	@Override
	public long scard(String key) {
		try (Jedis con = dbhandle.getConn()) {
			return con.scard(key.getBytes(StandardCharsets.UTF_8));
		}
	}

}
